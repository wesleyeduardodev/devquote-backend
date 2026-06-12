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
import java.util.function.Supplier;

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
        runSync("Sincronizacao PRs Git", deliveryItemRepository::findEligibleForGitSync);
    }

    @Override
    @Transactional
    public void syncMergedPullRequestsDevelopmentFlow() {
        runSync("Sincronizacao PRs Git (fluxo DESENVOLVIMENTO)",
                deliveryItemRepository::findEligibleForGitSyncDevelopment);
    }

    private void runSync(String label, Supplier<List<DeliveryItem>> itemsSupplier) {
        long startTime = System.currentTimeMillis();
        log.info("=== INICIO: {} ===", label);

        if (!parameterHelper.isIntegrationEnabled()) {
            log.info("Integracao com Git desabilitada. Pulando sincronizacao.");
            return;
        }

        List<DeliveryItem> eligibleItems = itemsSupplier.get();
        log.info("Encontrados {} itens elegiveis para verificacao", eligibleItems.size());

        if (eligibleItems.isEmpty()) {
            log.info("Nenhum item elegivel para sincronizacao");
            log.info("=== FIM: {} | Nenhum item para processar ===", label);
            return;
        }

        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (DeliveryItem item : eligibleItems) {
            String taskCode = item.getDelivery() != null && item.getDelivery().getTask() != null
                    ? item.getDelivery().getTask().getCode() : "N/A";
            Long deliveryId = item.getDelivery() != null ? item.getDelivery().getId() : null;
            String prUrl = item.getPullRequest() != null ? item.getPullRequest() : "N/A";

            log.info("[PROCESSANDO] DeliveryItem ID: {}, Delivery ID: {}, Task Code: {}, PR: {}",
                    item.getId(), deliveryId, taskCode, prUrl);

            try {
                DeliveryStatus statusAnterior = item.getStatus();
                boolean updated = processDeliveryItem(item);

                if (updated) {
                    updatedCount.incrementAndGet();
                    log.info("[SUCESSO] DeliveryItem ID: {}, Task Code: {} | merged: false -> true | Status: {} -> PRODUCTION",
                            item.getId(), taskCode, statusAnterior);
                } else {
                    skippedCount.incrementAndGet();
                    log.info("[PULADO] DeliveryItem ID: {}, Task Code: {} | Motivo: PR ainda nao mergeado",
                            item.getId(), taskCode);
                }
            } catch (GitProviderException e) {
                if ("UNSUPPORTED_PROVIDER".equals(e.getCode())) {
                    skippedCount.incrementAndGet();
                    log.warn("[PULADO] DeliveryItem ID: {}, Task Code: {} | Motivo: Provedor nao suportado",
                            item.getId(), taskCode);
                } else {
                    errorCount.incrementAndGet();
                    log.error("[ERRO] DeliveryItem ID: {}, Task Code: {} | Motivo: {} - {}",
                            item.getId(), taskCode, e.getCode(), e.getMessage());
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("[ERRO] DeliveryItem ID: {}, Task Code: {} | Motivo: {}",
                        item.getId(), taskCode, e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== FIM: {} | Atualizados: {}, Erros: {}, Pulados: {}, Total: {} ({}ms) ===",
                label, updatedCount.get(), errorCount.get(), skippedCount.get(), eligibleItems.size(), duration);
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

    @Override
    @Transactional
    public int syncMergedPullRequestsForDelivery(Long deliveryId) {
        if (!parameterHelper.isIntegrationEnabled()) {
            log.info("Integracao com Git desabilitada. Pulando sync de PRs da Delivery {}", deliveryId);
            return 0;
        }

        List<DeliveryItem> items = deliveryItemRepository.findEligibleForGitSyncByDeliveryId(deliveryId);
        if (items.isEmpty()) {
            log.info("[git-sync/delivery] Delivery {} | Nenhum item elegivel (sem PR ou ja mergeado)", deliveryId);
            return 0;
        }

        int updated = 0;
        for (DeliveryItem item : items) {
            try {
                if (processDeliveryItem(item)) {
                    updated++;
                    log.info("[git-sync/delivery] Delivery {} | Item {} merged -> PRODUCTION", deliveryId, item.getId());
                }
            } catch (GitProviderException e) {
                log.warn("[git-sync/delivery] Delivery {} | Item {} | Erro {}: {}",
                        deliveryId, item.getId(), e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[git-sync/delivery] Delivery {} | Item {} | Erro: {}",
                        deliveryId, item.getId(), e.getMessage());
            }
        }
        return updated;
    }

    private boolean processDeliveryItem(DeliveryItem item) {
        String prUrl = item.getPullRequest();

        if (prUrl == null || prUrl.trim().isEmpty()) {
            return false;
        }

        if (!gitProviderFactory.isSupported(prUrl)) {
            return false;
        }

        GitProviderClient provider = gitProviderFactory.getProvider(prUrl);
        boolean isMerged = provider.checkIfMerged(prUrl);

        if (isMerged) {
            updateDeliveryItemToProduction(item);
            return true;
        }

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
    }
}
