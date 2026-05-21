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
    public List<BoardTask> fetchPriorityTasks() {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        String orderFieldId = config.getClickUpOrderFieldId();
        List<Map<String, Object>> raw = clickUpClient.getListTasksFiltered(
                config.getClickUpListId(),
                config.getPriorityStatuses(),
                config.getClickUpDeveloperFieldId(),
                config.getClickUpDeveloperOptionId());

        List<BoardTask> result = new ArrayList<>();
        for (Map<String, Object> t : raw) {
            result.add(mapTask(t, orderFieldId));
        }
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

        return BoardTask.builder()
                .id(id)
                .name(name)
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
