package br.com.devquote.service.impl;

import br.com.devquote.client.clickup.ClickUpClient;
import br.com.devquote.entity.Delivery;
import br.com.devquote.enums.ClickUpStatusMapping;
import br.com.devquote.enums.FlowType;
import br.com.devquote.helper.ClickUpParameterHelper;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.ClickUpSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickUpSyncServiceImpl implements ClickUpSyncService {

    private final DeliveryRepository deliveryRepository;
    private final ClickUpClient clickUpClient;
    private final ClickUpParameterHelper parameterHelper;

    @Override
    @Transactional
    public void syncDeliveriesToClickUp() {
        if (!parameterHelper.isIntegrationEnabled()) {
            log.info("Integracao com ClickUp desabilitada. Pulando sincronizacao.");
            return;
        }

        log.info("Iniciando sincronizacao de entregas com ClickUp");

        List<Delivery> eligibleDeliveries = deliveryRepository.findEligibleForClickUpSync(
                FlowType.DESENVOLVIMENTO.name()
        );

        log.info("Encontradas {} entregas elegiveis para sincronizacao", eligibleDeliveries.size());

        if (eligibleDeliveries.isEmpty()) {
            log.info("Nenhuma entrega elegivel para sincronizacao");
            return;
        }

        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (Delivery delivery : eligibleDeliveries) {
            try {
                boolean updated = processDelivery(delivery);
                if (updated) {
                    updatedCount.incrementAndGet();
                } else {
                    skippedCount.incrementAndGet();
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("Erro ao processar Delivery {}: {}", delivery.getId(), e.getMessage(), e);
            }
        }

        log.info("Sincronizacao concluida: {} atualizados, {} erros, {} pulados de {} total",
                updatedCount.get(), errorCount.get(), skippedCount.get(), eligibleDeliveries.size());
    }

    @Override
    @Transactional
    public boolean syncDeliveryToClickUp(Long deliveryId) {
        if (!parameterHelper.isIntegrationEnabled()) {
            log.warn("Integracao com ClickUp desabilitada");
            return false;
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery nao encontrada: " + deliveryId));

        return processDelivery(delivery);
    }

    private boolean processDelivery(Delivery delivery) {
        if (delivery.getTask() == null) {
            log.debug("Delivery {} nao possui Task associada", delivery.getId());
            return false;
        }

        String taskCode = delivery.getTask().getCode();
        if (taskCode == null || taskCode.trim().isEmpty()) {
            log.debug("Task da Delivery {} nao possui code", delivery.getId());
            return false;
        }

        if (!ClickUpStatusMapping.isSyncableStatus(delivery.getStatus())) {
            log.debug("Status {} da Delivery {} nao e sincronizavel", delivery.getStatus(), delivery.getId());
            return false;
        }

        String clickUpStatus = ClickUpStatusMapping.fromDeliveryStatus(delivery.getStatus());
        if (clickUpStatus == null) {
            log.debug("Nao foi possivel mapear status {} para ClickUp", delivery.getStatus());
            return false;
        }

        if (clickUpStatus.equals(delivery.getClickupLastSyncedStatus())) {
            log.debug("Delivery {} ja esta sincronizada com status '{}'", delivery.getId(), clickUpStatus);
            return false;
        }

        log.info("Sincronizando Delivery {} (Task {}) para status '{}'",
                delivery.getId(), taskCode, clickUpStatus);

        boolean success = clickUpClient.updateTaskStatus(taskCode, clickUpStatus);

        if (success) {
            delivery.setClickupLastSyncedStatus(clickUpStatus);
            delivery.setClickupSyncedAt(LocalDateTime.now());
            deliveryRepository.save(delivery);

            log.info("Delivery {} sincronizada com sucesso. ClickUp status: '{}'",
                    delivery.getId(), clickUpStatus);
            return true;
        }

        log.warn("Falha ao sincronizar Delivery {} com ClickUp", delivery.getId());
        return false;
    }
}
