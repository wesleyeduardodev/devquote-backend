package br.com.devquote.client.board.impl;

import br.com.devquote.client.board.BoardTask;
import br.com.devquote.client.board.TaskBoardProvider;
import br.com.devquote.client.clickup.ClickUpClient;
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
        return fetchPriorityTasks(false);
    }

    @Override
    public List<BoardTask> fetchPriorityTasks(boolean includeAssignee) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        long t0 = System.currentTimeMillis();
        String orderFieldId = config.getClickUpOrderFieldId();
        String listId = config.getClickUpListId();

        // 1) Sempre busca pelo custom field Desenvolvedor (prioridade primária).
        long tDev0 = System.currentTimeMillis();
        List<Map<String, Object>> rawByDev = clickUpClient.getListTasksFiltered(
                listId,
                null,
                config.getClickUpDeveloperFieldId(),
                config.getClickUpDeveloperOptionId());
        long tDev = System.currentTimeMillis() - tDev0;
        log.info("[priority-board] Dev field trouxe {} tarefa(s) em {}ms", rawByDev.size(), tDev);

        // 2) Se o caller pediu pra incluir Responsável e há userId, busca também e faz UNION dedup.
        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> t : rawByDev) {
            Object id = t.get("id");
            if (id != null) merged.put(id.toString(), t);
        }
        if (includeAssignee) {
            // Prioridade: override no parâmetro CLICKUP_BOARD_ASSIGNEE_USER_ID.
            // Fallback automático: user dono do token (descoberto via /api/v2/user).
            String userId = config.getBoardAssigneeUserId();
            String origem = "param";
            if (userId == null || userId.trim().isEmpty()) {
                Map<String, Object> currentUser = clickUpClient.getCurrentUser();
                if (currentUser != null && currentUser.get("id") != null) {
                    userId = currentUser.get("id").toString();
                    origem = "auto-detect (token)";
                }
            }
            if (userId != null && !userId.trim().isEmpty()) {
                long tAs0 = System.currentTimeMillis();
                List<Map<String, Object>> rawByAssignee = clickUpClient.getListTasksByAssignee(
                        listId, null, userId);
                long tAs = System.currentTimeMillis() - tAs0;
                int novosUnicos = 0;
                for (Map<String, Object> t : rawByAssignee) {
                    Object id = t.get("id");
                    if (id == null) continue;
                    if (merged.putIfAbsent(id.toString(), t) == null) novosUnicos++;
                }
                log.info("[priority-board] Assignee userId={} ({}) trouxe {} tarefa(s) em {}ms — {} novas após dedup",
                        userId, origem, rawByAssignee.size(), tAs, novosUnicos);
            } else {
                log.info("[priority-board] includeAssignee=true mas sem userId disponível — pulando 2ª chamada");
            }
        }

        List<BoardTask> result = new ArrayList<>(merged.size());
        for (Map<String, Object> t : merged.values()) {
            result.add(mapTask(t, orderFieldId));
        }
        log.info("[priority-board] Total final: {} tarefa(s) únicas em {}ms (includeAssignee={})",
                result.size(), System.currentTimeMillis() - t0, includeAssignee);
        return result;
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
