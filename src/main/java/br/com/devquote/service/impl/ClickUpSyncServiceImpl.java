package br.com.devquote.service.impl;

import br.com.devquote.client.clickup.ClickUpClient;
import br.com.devquote.dto.response.SyncPullRequestsResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.enums.ClickUpStatusMapping;
import br.com.devquote.enums.ClickUpStatusOrder;
import br.com.devquote.enums.FlowType;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.helper.ClickUpParameterHelper;
import br.com.devquote.helper.TaskBoardParameterHelper;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.ClickUpSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickUpSyncServiceImpl implements ClickUpSyncService {

    private final DeliveryRepository deliveryRepository;
    private final ClickUpClient clickUpClient;
    private final ClickUpParameterHelper parameterHelper;
    private final TaskBoardParameterHelper boardConfig;

    private static final String PR_BLOCK_HEADER = "PRs da entrega:";

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

        String newClickUpStatus = ClickUpStatusMapping.fromDeliveryStatus(delivery.getStatus());
        if (newClickUpStatus == null) {
            log.info("[PULADO] Delivery ID: {}, Task Code: {} | Motivo: Nao foi possivel mapear status {}",
                    delivery.getId(), taskCode, delivery.getStatus());
            return false;
        }

        String currentClickUpStatus = clickUpClient.getTaskStatus(taskCode);
        if (currentClickUpStatus == null) {
            log.warn("[PULADO] Delivery ID: {}, Task Code: {} | Motivo: Nao foi possivel obter status atual do ClickUp",
                    delivery.getId(), taskCode);
            return false;
        }

        if (!ClickUpStatusOrder.canAdvanceTo(currentClickUpStatus, newClickUpStatus)) {
            log.info("[PRESERVADO] Delivery ID: {}, Task Code: {} | Status ClickUp atual '{}' esta mais avancado que '{}'. Nao sera regredido.",
                    delivery.getId(), taskCode, currentClickUpStatus, newClickUpStatus);
            delivery.setClickupLastSyncedStatus(currentClickUpStatus);
            delivery.setClickupSyncedAt(LocalDateTime.now());
            deliveryRepository.save(delivery);
            return false;
        }

        boolean success = clickUpClient.updateTaskStatus(taskCode, newClickUpStatus);

        if (success) {
            delivery.setClickupLastSyncedStatus(newClickUpStatus);
            delivery.setClickupSyncedAt(LocalDateTime.now());
            deliveryRepository.save(delivery);

            // PRs NÃO são propagados aqui automaticamente — sync de PR é manual,
            // disparado pelo botão "Atualizar Branch" em /deliveries (endpoint próprio).

            log.info("[SUCESSO] Delivery ID: {}, Task Code: {} | Status: '{}' -> '{}'",
                    delivery.getId(), taskCode, currentClickUpStatus, newClickUpStatus);
            return true;
        }

        log.error("[ERRO] Delivery ID: {}, Task Code: {} | Motivo: Falha na API do ClickUp",
                delivery.getId(), taskCode);
        return false;
    }

    /**
     * Endpoint público manual (chamado pelo botão "Atualizar Branch" na UI).
     * Carrega a delivery, valida, e dispara o sync de PRs com no-op detection.
     */
    @Override
    @Transactional(readOnly = true)
    public SyncPullRequestsResponse syncPullRequestsForDelivery(Long deliveryId) {
        if (!parameterHelper.isIntegrationEnabled()) {
            return SyncPullRequestsResponse.builder()
                    .pullRequestCount(0)
                    .message("Integração com ClickUp está desabilitada (CLICKUP_INTEGRATION_ENABLED).")
                    .build();
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery não encontrada: " + deliveryId));

        if (delivery.getTask() == null) {
            return SyncPullRequestsResponse.builder()
                    .pullRequestCount(0)
                    .message("Esta entrega não está associada a uma tarefa.")
                    .build();
        }
        String taskCode = delivery.getTask().getCode();
        if (taskCode == null || taskCode.isBlank()) {
            return SyncPullRequestsResponse.builder()
                    .pullRequestCount(0)
                    .message("Tarefa sem código do ClickUp.")
                    .build();
        }

        return syncPullRequestsToClickUp(delivery, taskCode);
    }

    /**
     * Propaga os PRs dos DeliveryItems pro ClickUp:
     *   - Campo Branch (custom field text): preenchido com a URL quando há
     *     EXATAMENTE 1 PR; limpo quando há 0 ou 2+ PRs (com 2+ fica ruim concatenado).
     *   - Comentário na task: cria um novo SÓ quando há 2+ PRs (com 1 PR o campo
     *     Branch sozinho já dá conta; com 0, nada a dizer). NUNCA toca a descrição.
     */
    private SyncPullRequestsResponse syncPullRequestsToClickUp(Delivery delivery, String taskCode) {
        List<DeliveryItem> withPr = (delivery.getItems() == null)
                ? List.of()
                : delivery.getItems().stream()
                        .filter(i -> i.getPullRequest() != null && !i.getPullRequest().isBlank())
                        .collect(Collectors.toList());
        int prCount = withPr.size();

        String branchValue = prCount == 1 ? withPr.get(0).getPullRequest() : "";

        boolean branchUpdated = false;
        boolean commentCreated = false;

        String branchFieldId = boardConfig.getClickUpBranchFieldId();
        boolean hasBranchField = branchFieldId != null && !branchFieldId.isBlank();
        if (hasBranchField) {
            branchUpdated = clickUpClient.updateTaskCustomField(taskCode, branchFieldId, branchValue);
            if (branchUpdated) {
                log.info("[branch-sync] Delivery {} | Campo Branch atualizado ({} PR(s))", delivery.getId(), prCount);
            } else {
                log.warn("[branch-sync] Delivery {} | Falha ao atualizar campo Branch", delivery.getId());
            }
        }

        if (prCount >= 2) {
            List<Map<String, Object>> blocks = buildPrCommentBlocks(withPr);
            String newId = clickUpClient.createTaskComment(taskCode, blocks);
            if (newId != null) {
                commentCreated = true;
                log.info("[branch-sync] Delivery {} | Comentario de PRs criado (id={})", delivery.getId(), newId);
            } else {
                log.warn("[branch-sync] Delivery {} | Falha ao criar comentario de PRs", delivery.getId());
            }
        } else {
            log.info("[branch-sync] Delivery {} | {} PR(s) — comentario nao publicado (so Branch)",
                    delivery.getId(), prCount);
        }

        return SyncPullRequestsResponse.builder()
                .branchUpdated(branchUpdated)
                .commentUpdated(commentCreated)
                .pullRequestCount(prCount)
                .message(buildMessage(prCount, branchUpdated, commentCreated, hasBranchField))
                .build();
    }

    /**
     * Monta o array de blocos do comentário do ClickUp (formato "comment block array"):
     *   - Header "PRs da entrega:" em negrito
     *   - Linha em branco entre o header e cada PR (visual mais espaçado)
     *   - Cada PR: "Projeto: url" (ClickUp auto-detecta URL e renderiza como link)
     */
    private static List<Map<String, Object>> buildPrCommentBlocks(List<DeliveryItem> withPr) {
        List<Map<String, Object>> blocks = new ArrayList<>();

        Map<String, Object> headerAttrs = new HashMap<>();
        headerAttrs.put("bold", true);
        blocks.add(textBlock(PR_BLOCK_HEADER, headerAttrs));

        for (DeliveryItem item : withPr) {
            String projectName = (item.getProject() != null && item.getProject().getName() != null)
                    ? item.getProject().getName() : "(sem projeto)";
            blocks.add(textBlock("\n\n" + projectName + ": " + item.getPullRequest(), null));
        }

        return blocks;
    }

    private static Map<String, Object> textBlock(String text, Map<String, Object> attributes) {
        Map<String, Object> block = new HashMap<>();
        block.put("text", text);
        if (attributes != null && !attributes.isEmpty()) {
            block.put("attributes", attributes);
        }
        return block;
    }

    private static String buildMessage(int prCount, boolean branchUpdated, boolean commentCreated, boolean hasBranchField) {
        if (prCount == 0) {
            return branchUpdated ? "Campo Branch limpo (entrega sem PRs)." : "Nada a sincronizar (sem PRs nos items).";
        }
        StringBuilder sb = new StringBuilder("Sincronizado: " + prCount + (prCount == 1 ? " PR." : " PRs."));
        if (hasBranchField && branchUpdated) {
            sb.append(prCount == 1 ? " Campo Branch atualizado." : " Campo Branch limpo (regra: so recebe com 1 PR).");
        }
        if (commentCreated) sb.append(" Comentario publicado.");
        return sb.toString();
    }
}
