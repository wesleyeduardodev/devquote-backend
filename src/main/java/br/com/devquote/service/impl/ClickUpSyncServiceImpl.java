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
        long startTime = System.currentTimeMillis();
        log.info("=== INICIO: Sincronizacao ClickUp ===");

        if (!parameterHelper.isIntegrationEnabled()) {
            log.info("Integracao com ClickUp desabilitada. Pulando sincronizacao.");
            return;
        }

        List<Delivery> eligibleDeliveries = deliveryRepository.findEligibleForClickUpSync(
                FlowType.DESENVOLVIMENTO.name()
        );

        log.info("Encontradas {} entregas elegiveis para sincronizacao", eligibleDeliveries.size());

        if (eligibleDeliveries.isEmpty()) {
            log.info("Nenhuma entrega elegivel para sincronizacao");
            log.info("=== FIM: Sincronizacao ClickUp | Nenhuma entrega para processar ===");
            return;
        }

        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        for (Delivery delivery : eligibleDeliveries) {
            String taskCode = delivery.getTask() != null ? delivery.getTask().getCode() : null;

            log.info("[PROCESSANDO] Delivery ID: {}, Task Code: {}",
                    delivery.getId(), taskCode != null ? taskCode : "N/A");

            try {
                String statusAnterior = delivery.getClickupLastSyncedStatus();
                boolean updated = processDelivery(delivery);

                if (updated) {
                    updatedCount.incrementAndGet();
                } else {
                    skippedCount.incrementAndGet();
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                log.error("[ERRO] Delivery ID: {}, Task Code: {} | Motivo: {}",
                        delivery.getId(), taskCode != null ? taskCode : "N/A", e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== FIM: Sincronizacao ClickUp | Atualizados: {}, Erros: {}, Pulados: {}, Total: {} ({}ms) ===",
                updatedCount.get(), errorCount.get(), skippedCount.get(), eligibleDeliveries.size(), duration);
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
        String taskCode = delivery.getTask() != null ? delivery.getTask().getCode() : null;

        if (delivery.getTask() == null) {
            log.info("[PULADO] Delivery ID: {} | Motivo: Task nao associada", delivery.getId());
            return false;
        }

        if (taskCode == null || taskCode.trim().isEmpty()) {
            log.info("[PULADO] Delivery ID: {} | Motivo: Task sem codigo ClickUp", delivery.getId());
            return false;
        }

        if (!ClickUpStatusMapping.isSyncableStatus(delivery.getStatus())) {
            log.info("[PULADO] Delivery ID: {}, Task Code: {} | Motivo: Status {} nao sincronizavel",
                    delivery.getId(), taskCode, delivery.getStatus());
            return false;
        }

        String clickUpStatus = ClickUpStatusMapping.fromDeliveryStatus(delivery.getStatus());
        if (clickUpStatus == null) {
            log.info("[PULADO] Delivery ID: {}, Task Code: {} | Motivo: Nao foi possivel mapear status {}",
                    delivery.getId(), taskCode, delivery.getStatus());
            return false;
        }

        String statusAnterior = delivery.getClickupLastSyncedStatus();
        if (clickUpStatus.equals(statusAnterior)) {
            log.info("[PULADO] Delivery ID: {}, Task Code: {} | Motivo: Status ja sincronizado ({})",
                    delivery.getId(), taskCode, clickUpStatus);
            return false;
        }

        boolean success = clickUpClient.updateTaskStatus(taskCode, clickUpStatus);

        if (success) {
            delivery.setClickupLastSyncedStatus(clickUpStatus);
            delivery.setClickupSyncedAt(LocalDateTime.now());
            deliveryRepository.save(delivery);

            log.info("[SUCESSO] Delivery ID: {}, Task Code: {} | Status: {} -> {}",
                    delivery.getId(), taskCode,
                    statusAnterior != null ? statusAnterior : "null",
                    clickUpStatus);
            return true;
        }

        log.error("[ERRO] Delivery ID: {}, Task Code: {} | Motivo: Falha na API do ClickUp",
                delivery.getId(), taskCode);
        return false;
    }
}
