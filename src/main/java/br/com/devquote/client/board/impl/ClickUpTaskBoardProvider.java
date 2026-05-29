package br.com.devquote.client.board.impl;

import br.com.devquote.client.board.BoardTask;
import br.com.devquote.client.board.TaskBoardProvider;
import br.com.devquote.client.clickup.ClickUpClient;
import br.com.devquote.enums.BoardFilterMode;
import br.com.devquote.helper.ClickUpParameterHelper;
import br.com.devquote.helper.TaskBoardParameterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpTaskBoardProvider implements TaskBoardProvider {

    private final ClickUpClient clickUpClient;
    private final ClickUpParameterHelper clickUpParameterHelper;
    private final TaskBoardParameterHelper config;

    @Override
    public String getProviderName() {
        return "CLICKUP";
    }

    @Override
    public boolean supports(String providerKey) {
        return "CLICKUP".equalsIgnoreCase(providerKey);
    }

    @Override
    public boolean isConfigured() {
        return clickUpParameterHelper.isIntegrationEnabled()
                && notBlank(config.getClickUpListId())
                && notBlank(config.getClickUpDeveloperFieldId())
                && notBlank(config.getClickUpDeveloperOptionId());
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        if (!clickUpParameterHelper.isIntegrationEnabled()) return null;
        return clickUpClient.getCurrentUser();
    }

    @Override
    public List<BoardTask> fetchPriorityTasks() {
        return fetchPriorityTasks(BoardFilterMode.DEV_OR_ASSIGNEE);
    }

    @Override
    public List<BoardTask> fetchPriorityTasks(BoardFilterMode mode) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }
        if (mode == null) mode = BoardFilterMode.DEV_OR_ASSIGNEE;

        long t0 = System.currentTimeMillis();
        String orderFieldId = config.getClickUpOrderFieldId();
        String listId = config.getClickUpListId();

        // Os 3 modos são uma partição por Desenvolvedor × Responsável, então preciso dos DOIS
        // conjuntos pra calcular interseção/diferença. Mapas keyed by id do ClickUp.
        long tDev0 = System.currentTimeMillis();
        Map<String, Map<String, Object>> devTasks = new LinkedHashMap<>();
        for (Map<String, Object> t : clickUpClient.getListTasksFiltered(
                listId, null, config.getClickUpDeveloperFieldId(), config.getClickUpDeveloperOptionId())) {
            Object id = t.get("id");
            if (id != null) devTasks.put(id.toString(), t);
        }
        log.info("[priority-board] Dev field trouxe {} tarefa(s) em {}ms", devTasks.size(), System.currentTimeMillis() - tDev0);

        Map<String, Map<String, Object>> assigneeTasks = new LinkedHashMap<>();
        String userId = resolveAssigneeUserId();
        if (userId != null) {
            long tAs0 = System.currentTimeMillis();
            for (Map<String, Object> t : clickUpClient.getListTasksByAssignee(listId, null, userId)) {
                Object id = t.get("id");
                if (id != null) assigneeTasks.put(id.toString(), t);
            }
            log.info("[priority-board] Assignee userId={} trouxe {} tarefa(s) em {}ms", userId, assigneeTasks.size(), System.currentTimeMillis() - tAs0);
        } else {
            log.info("[priority-board] sem userId de Responsável disponível — conjunto assignee vazio");
        }

        // Combina conforme o modo. Preserva a ordem de inserção da fonte (dev primeiro).
        Map<String, Map<String, Object>> selected = new LinkedHashMap<>();
        switch (mode) {
            case DEV_OR_ASSIGNEE:
                selected.putAll(devTasks);
                for (Map.Entry<String, Map<String, Object>> e : assigneeTasks.entrySet()) {
                    selected.putIfAbsent(e.getKey(), e.getValue());
                }
                break;
            case DEV_NOT_ASSIGNEE:
                for (Map.Entry<String, Map<String, Object>> e : devTasks.entrySet()) {
                    if (!assigneeTasks.containsKey(e.getKey())) selected.put(e.getKey(), e.getValue());
                }
                break;
            case ASSIGNEE_NOT_DEV:
                for (Map.Entry<String, Map<String, Object>> e : assigneeTasks.entrySet()) {
                    if (!devTasks.containsKey(e.getKey())) selected.put(e.getKey(), e.getValue());
                }
                break;
            case DEV_AND_ASSIGNEE:
            default:
                for (Map.Entry<String, Map<String, Object>> e : devTasks.entrySet()) {
                    if (assigneeTasks.containsKey(e.getKey())) selected.put(e.getKey(), e.getValue());
                }
                break;
        }

        List<BoardTask> result = new ArrayList<>(selected.size());
        for (Map<String, Object> t : selected.values()) {
            result.add(mapTask(t, orderFieldId));
        }
        log.info("[priority-board] Total final: {} tarefa(s) em {}ms (mode={})",
                result.size(), System.currentTimeMillis() - t0, mode);
        return result;
    }

    /**
     * Resolve o userId do Responsável: override em CLICKUP_BOARD_ASSIGNEE_USER_ID;
     * fallback automático no dono do token (via /api/v2/user). Null se indisponível.
     */
    private String resolveAssigneeUserId() {
        String userId = config.getBoardAssigneeUserId();
        if (userId != null && !userId.trim().isEmpty()) {
            return userId;
        }
        Map<String, Object> currentUser = clickUpClient.getCurrentUser();
        if (currentUser != null && currentUser.get("id") != null) {
            return currentUser.get("id").toString();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BoardTask mapTask(Map<String, Object> t, String orderFieldId) {
        String id = asString(t.get("id"));
        String name = asString(t.get("name"));

        String statusName = null;
        Object statusObj = t.get("status");
        if (statusObj instanceof Map) {
            statusName = asString(((Map<String, Object>) statusObj).get("status"));
        }

        String priority = null;
        Object prioObj = t.get("priority");
        if (prioObj instanceof Map) {
            priority = asString(((Map<String, Object>) prioObj).get("priority"));
        }

        String description = asString(t.get("description"));
        if (description == null || description.trim().isEmpty()) {
            description = asString(t.get("text_content"));
        }

        return BoardTask.builder()
                .id(id)
                .name(name)
                .description(description)
                .url("https://app.clickup.com/t/" + id)
                .statusName(statusName)
                .orderValue(extractOrder(t, orderFieldId))
                .priority(priority)
                .tags(extractTags(t))
                .build();
    }

    @SuppressWarnings("unchecked")
    private Double extractOrder(Map<String, Object> t, String orderFieldId) {
        if (orderFieldId == null || orderFieldId.trim().isEmpty()) {
            return null;
        }
        Object cf = t.get("custom_fields");
        if (!(cf instanceof List)) {
            return null;
        }
        for (Object o : (List<Object>) cf) {
            if (!(o instanceof Map)) {
                continue;
            }
            Map<String, Object> field = (Map<String, Object>) o;
            if (orderFieldId.equals(field.get("id"))) {
                Object value = field.get("value");
                if (value == null) {
                    return null;
                }
                try {
                    return Double.parseDouble(value.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractTags(Map<String, Object> t) {
        List<String> tags = new ArrayList<>();
        Object tagsObj = t.get("tags");
        if (tagsObj instanceof List) {
            for (Object o : (List<Object>) tagsObj) {
                if (o instanceof Map) {
                    String name = asString(((Map<String, Object>) o).get("name"));
                    if (name != null && !name.isEmpty()) {
                        tags.add(name);
                    }
                }
            }
        }
        return tags;
    }

    private String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
