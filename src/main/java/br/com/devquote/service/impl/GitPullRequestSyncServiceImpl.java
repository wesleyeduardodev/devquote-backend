package br.com.devquote.service.impl;

import br.com.devquote.client.git.GitProviderClient;
import br.com.devquote.client.git.GitProviderFactory;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.error.GitProviderException;
import br.com.devquote.helper.GitIntegrationParameterHelper;
import br.com.devquote.repository.DeliveryItemRepository;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.GitPullRequestSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitPullRequestSyncServiceImpl implements GitPullRequestSyncService {

    private final DeliveryItemRepository deliveryItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final GitProviderFactory gitProviderFactory;
    private final GitIntegrationParameterHelper parameterHelper;

    @Override
    @Transactional
    public void syncMergedPullRequests() {
        if (!parameterHelper.isIntegrationEnabled()) {
            log.info("Integracao com Git desabilitada. Pulando sincronizacao.");
            return;
        }

        log.info("Iniciando sincronizacao de PRs mergeados");

        List<DeliveryItem> eligibleItems = deliveryItemRepository.findEligibleForGitSync();
        log.info("Encontrados {} itens elegiveis para verificacao", eligibleItems.size());

        if (eligibleItems.isEmpty()) {
            log.info("Nenhum item elegivel para sincronizacao");
            return;
        }

        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (DeliveryItem item : eligibleItems) {
            try {
                boolean updated = processDeliveryItem(item);
                if (updated) {
                    updatedCount.incrementAndGet();
                }
            } catch (GitProviderException e) {
                if ("UNSUPPORTED_PROVIDER".equals(e.getCode())) {
                    skippedCount.incrementAndGet();
                    log.debug("Provedor nao suportado para item {}: {}", item.getId(), item.getPullRequest());
                } else {
                    errorCount.incrementAndGet();
                    log.error("Erro ao processar DeliveryItem {}: {} - {}",
                            item.getId(), e.getCode(), e.getMessage());
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("Erro inesperado ao processar DeliveryItem {}: {}",
                        item.getId(), e.getMessage(), e);
            }
        }

        log.info("Sincronizacao concluida: {} atualizados, {} erros, {} pulados de {} total",
                updatedCount.get(), errorCount.get(), skippedCount.get(), eligibleItems.size());
    }

    @Override
    @Async("gitSyncTaskExecutor")
    public void syncMergedPullRequestsAsync() {
        log.info("Iniciando sincronizacao assincrona de PRs mergeados");
        syncMergedPullRequests();
    }

    @Override
    @Transactional
    public boolean checkAndUpdatePullRequestStatus(Long deliveryItemId) {
        if (!parameterHelper.isIntegrationEnabled()) {
            log.warn("Integracao com Git desabilitada");
            return false;
        }

        DeliveryItem item = deliveryItemRepository.findById(deliveryItemId)
                .orElseThrow(() -> new RuntimeException("DeliveryItem nao encontrado: " + deliveryItemId));

        return processDeliveryItem(item);
    }

    private boolean processDeliveryItem(DeliveryItem item) {
        String prUrl = item.getPullRequest();

        if (prUrl == null || prUrl.trim().isEmpty()) {
            log.debug("URL de PR vazia para item {}", item.getId());
            return false;
        }

        if (!gitProviderFactory.isSupported(prUrl)) {
            log.debug("Provedor nao suportado para URL: {}", prUrl);
            return false;
        }

        GitProviderClient provider = gitProviderFactory.getProvider(prUrl);
        log.debug("Usando provedor {} para verificar PR: {}", provider.getProviderName(), prUrl);

        boolean isMerged = provider.checkIfMerged(prUrl);

        if (isMerged) {
            log.info("PR mergeado detectado: {} - Atualizando DeliveryItem {} para PRODUCTION",
                    prUrl, item.getId());
            updateDeliveryItemToProduction(item);
            return true;
        }

        log.debug("PR {} ainda nao foi mergeado", prUrl);
        return false;
    }

    private void updateDeliveryItemToProduction(DeliveryItem item) {
        item.setMerged(true);
        item.setMergedAt(LocalDateTime.now());
        item.setStatus(DeliveryStatus.PRODUCTION);

        if (item.getFinishedAt() == null) {
            item.setFinishedAt(LocalDateTime.now());
        }

        deliveryItemRepository.save(item);

        Delivery delivery = item.getDelivery();
        delivery.updateStatus();
        delivery.updateDates();
        deliveryRepository.save(delivery);

        log.info("DeliveryItem {} atualizado para PRODUCTION. Delivery {} status: {}",
                item.getId(), delivery.getId(), delivery.getStatus());
    }
}
