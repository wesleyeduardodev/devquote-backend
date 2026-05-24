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
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final String PR_BLOCK_HEADER = "**PRs da entrega:**";

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
     * Propaga os PRs dos DeliveryItems pro ClickUp com no-op detection:
     *   - Campo Branch (custom field text): SÓ preenchido quando há EXATAMENTE 1 PR
     *     (com 2+ PRs fica visualmente ruim concatenado; usa a descrição pra esses)
     *   - Descrição da task: bloco "**PRs da entrega:**" + bullets "- Projeto: url"
     *
     * Antes de escrever, lê o estado atual da task no ClickUp e compara — só chama
     * a API de write se algo realmente mudou. Suporta também o cenário de REMOÇÃO:
     * se nenhum item tem PR (todos foram removidos), limpa o campo Branch e remove
     * o bloco da descrição.
     */
    private SyncPullRequestsResponse syncPullRequestsToClickUp(Delivery delivery, String taskCode) {
        List<DeliveryItem> withPr = (delivery.getItems() == null)
                ? List.of()
                : delivery.getItems().stream()
                        .filter(i -> i.getPullRequest() != null && !i.getPullRequest().isBlank())
                        .collect(Collectors.toList());
        int prCount = withPr.size();

        // 1a) Campo Branch — só preenche com 1 PR. Com 0 ou 2+, vai vazio (limpa).
        String branchValue = prCount == 1 ? withPr.get(0).getPullRequest() : "";

        // 1b) Bloco da descrição — bullets "- Projeto: url" (vazio se sem PRs → remove bloco)
        String descBlockBody = withPr.stream()
                .map(i -> {
                    String projectName = (i.getProject() != null && i.getProject().getName() != null)
                            ? i.getProject().getName() : "(sem projeto)";
                    return "- " + projectName + ": " + i.getPullRequest();
                })
                .collect(Collectors.joining("\n"));

        // 2) Lê a task atual do ClickUp (uma vez, pra usar nas duas comparações)
        Map<String, Object> taskMap = clickUpClient.getTask(taskCode);
        if (taskMap == null) {
            throw new ResourceNotFoundException("Tarefa não encontrada no ClickUp: " + taskCode);
        }

        boolean branchUpdated = false;
        boolean descriptionUpdated = false;

        // 3) Campo Branch — só escreve se mudou e se field está configurado
        String branchFieldId = boardConfig.getClickUpBranchFieldId();
        if (branchFieldId != null && !branchFieldId.isBlank()) {
            String currentBranchValue = readCustomFieldValue(taskMap, branchFieldId);
            if (!Objects.equals(safe(currentBranchValue), safe(branchValue))) {
                boolean ok = clickUpClient.updateTaskCustomField(taskCode, branchFieldId, branchValue);
                if (ok) {
                    branchUpdated = true;
                    log.info("[branch-sync] Delivery {} | Campo Branch atualizado ({} PR(s))", delivery.getId(), prCount);
                } else {
                    log.warn("[branch-sync] Delivery {} | Falha ao atualizar campo Branch", delivery.getId());
                }
            } else {
                log.debug("[branch-sync] Delivery {} | Campo Branch já sincronizado, no-op", delivery.getId());
            }
        }

        // 4) Descrição — só anexa o bloco se há PRs (sem strip/idempotência: se o user
        // clicar 3x, vão aparecer 3 blocos — comportamento esperado. Pra atualizar, o
        // user apaga o trecho manualmente no ClickUp antes do próximo sync).
        // Se prCount==0, não toca na descrição.
        if (prCount > 0) {
            String currentDesc = (String) taskMap.get("markdown_description");
            if (currentDesc == null) currentDesc = (String) taskMap.get("description");
            String newDesc = appendPrBlock(currentDesc, descBlockBody);
            boolean ok = clickUpClient.updateTaskDescription(taskCode, newDesc);
            if (ok) {
                descriptionUpdated = true;
                log.info("[branch-sync] Delivery {} | Bloco de PRs anexado na descrição", delivery.getId());
            } else {
                log.warn("[branch-sync] Delivery {} | Falha ao atualizar descrição", delivery.getId());
            }
        } else {
            log.info("[branch-sync] Delivery {} | Sem PRs — descrição não modificada", delivery.getId());
        }

        return SyncPullRequestsResponse.builder()
                .branchUpdated(branchUpdated)
                .descriptionUpdated(descriptionUpdated)
                .pullRequestCount(prCount)
                .message(buildMessage(prCount, branchUpdated, descriptionUpdated,
                        branchFieldId != null && !branchFieldId.isBlank()))
                .build();
    }

    private static String safe(String s) { return s == null ? "" : s; }

    @SuppressWarnings("unchecked")
    private static String readCustomFieldValue(Map<String, Object> taskMap, String fieldId) {
        Object cf = taskMap.get("custom_fields");
        if (!(cf instanceof List)) return "";
        for (Object o : (List<Object>) cf) {
            if (o instanceof Map) {
                Map<String, Object> field = (Map<String, Object>) o;
                if (fieldId.equals(field.get("id"))) {
                    Object value = field.get("value");
                    return value == null ? "" : value.toString();
                }
            }
        }
        return "";
    }

    private static String buildMessage(int prCount, boolean branchUpdated, boolean descUpdated, boolean hasBranchField) {
        if (prCount == 0) {
            if (branchUpdated) return "Campo Branch limpo (entrega sem PRs).";
            return "Nada a sincronizar (sem PRs nos items).";
        }
        if (prCount == 1) {
            StringBuilder sb = new StringBuilder("Sincronizado: 1 PR.");
            if (branchUpdated) sb.append(" Campo Branch atualizado.");
            if (descUpdated) sb.append(" Bloco anexado na descrição.");
            if (!hasBranchField && !branchUpdated) sb.append(" (Campo Branch não configurado — só descrição.)");
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder("Sincronizado: " + prCount + " PRs.");
        if (descUpdated) sb.append(" Bloco anexado na descrição.");
        if (branchUpdated) sb.append(" Campo Branch limpo (regra: Branch só recebe quando há 1 PR).");
        return sb.toString();
    }

    /**
     * Anexa o bloco de PRs no fim da descrição. Sem idempotência: cada chamada =
     * +1 bloco no fim (decisão consciente — usuário limpa manualmente se quiser
     * substituir, e isso evita complexidade de regex que falha com formatação do ClickUp).
     */
    static String appendPrBlock(String current, String descBlockBody) {
        String newBlock = PR_BLOCK_HEADER + "\n" + descBlockBody;

        if (current == null || current.isBlank()) {
            return newBlock;
        }

        String trimmed = current.replaceAll("\\s+$", "");
        return trimmed + "\n\n---\n" + newBlock;
    }
}
