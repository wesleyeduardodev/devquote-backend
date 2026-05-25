package br.com.devquote.client.clickup.impl;

import br.com.devquote.client.clickup.ClickUpClient;
import br.com.devquote.helper.ClickUpParameterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpClientImpl implements ClickUpClient {

    private static final String CLICKUP_API_BASE = "https://api.clickup.com/api/v2";

    private final RestTemplate clickUpRestTemplate;
    private final ClickUpParameterHelper parameterHelper;

    // Cache do current user — chaveado pelo token (se o token mudar, descobre de novo).
    private final Map<String, Map<String, Object>> currentUserCache = new ConcurrentHashMap<>();

    @Override
    public boolean updateTaskStatus(String taskId, String status) {
        String url = String.format("%s/task/%s", CLICKUP_API_BASE, taskId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Atualizando tarefa {} para status '{}'", taskId, status);

            ResponseEntity<String> response = clickUpRestTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Tarefa {} atualizada para '{}' com sucesso", taskId, status);
                return true;
            }

            log.warn("Resposta inesperada ao atualizar tarefa {}: {}", taskId, response.getStatusCode());
            return false;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Tarefa nao encontrada no ClickUp: {}", taskId);
            return false;

        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Acesso negado ao ClickUp para tarefa {}: {}", taskId, e.getMessage());
            return false;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP ao atualizar tarefa {} - Status: {} - Body: {}",
                    taskId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;

        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar tarefa {}: {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTask(String taskId) {
        String url = String.format("%s/task/%s", CLICKUP_API_BASE, taskId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = clickUpRestTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Erro ao buscar tarefa {}: {}", taskId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getTaskStatus(String taskId) {
        Map<String, Object> task = getTask(taskId);
        if (task == null) {
            return null;
        }

        Map<String, Object> statusInfo = (Map<String, Object>) task.get("status");
        if (statusInfo == null) {
            return null;
        }

        return (String) statusInfo.get("status");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getList(String listId) {
        String url = String.format("%s/list/%s", CLICKUP_API_BASE, listId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = clickUpRestTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Erro ao buscar list {}: {}", listId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAvailableStatuses(String taskId) {
        Map<String, Object> task = getTask(taskId);
        if (task == null) {
            return Collections.emptyList();
        }

        Map<String, Object> listInfo = (Map<String, Object>) task.get("list");
        if (listInfo == null) {
            return Collections.emptyList();
        }

        String listId = (String) listInfo.get("id");
        Map<String, Object> list = getList(listId);
        if (list == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> statuses = (List<Map<String, Object>>) list.get("statuses");
        if (statuses == null) {
            return Collections.emptyList();
        }

        List<String> statusNames = new ArrayList<>();
        for (Map<String, Object> status : statuses) {
            statusNames.add((String) status.get("status"));
        }

        return statusNames;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListTasksFiltered(String listId, List<String> statuses, String devFieldId, String devOptionId) {
        List<Map<String, Object>> all = new ArrayList<>();
        if (listId == null || listId.trim().isEmpty()) {
            return all;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String customFields = null;
        if (devFieldId != null && !devFieldId.trim().isEmpty()
                && devOptionId != null && !devOptionId.trim().isEmpty()) {
            customFields = "[{\"field_id\":\"" + devFieldId + "\",\"operator\":\"=\",\"value\":\"" + devOptionId + "\"}]";
        }

        for (int page = 0; page < 50; page++) {
            try {
                StringBuilder query = new StringBuilder();
                query.append("page=").append(page)
                        // include_closed=false: a API NÃO retorna tarefas "closed"
                        // (geralmente status COMPLETE/CONCLUÍDO). Isso evita trazer
                        // milhares de tarefas históricas que iriam ser filtradas
                        // depois pelo CLICKUP_HIDDEN_STATUSES — economia enorme de
                        // tempo de resposta.
                        .append("&include_closed=false")
                        // subtasks=true: a regra do board é "vem se for Desenvolvedor
                        // OU Responsável da task". Se filtrar subtasks fora, uma task
                        // que casa com o filtro mas mora debaixo de uma pai some do
                        // board. A API do ClickUp continua aplicando os filtros
                        // (custom_fields/assignees) também nas subtasks — então só
                        // vem o que de fato casa.
                        .append("&subtasks=true");
                if (statuses != null) {
                    for (String s : statuses) {
                        query.append("&statuses%5B%5D=").append(UriUtils.encodeQueryParam(s, StandardCharsets.UTF_8));
                    }
                }
                if (customFields != null) {
                    query.append("&custom_fields=").append(UriUtils.encodeQueryParam(customFields, StandardCharsets.UTF_8));
                }

                URI uri = URI.create(CLICKUP_API_BASE + "/list/" + listId + "/task?" + query);

                ResponseEntity<Map> response = clickUpRestTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    break;
                }
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) body.get("tasks");
                if (tasks == null || tasks.isEmpty()) {
                    break;
                }
                all.addAll(tasks);

                Object lastPage = body.get("last_page");
                if (Boolean.TRUE.equals(lastPage) || tasks.size() < 100) {
                    break;
                }
            } catch (Exception e) {
                log.error("Erro ao buscar tarefas da list {} (page {}): {}", listId, page, e.getMessage());
                break;
            }
        }

        return all;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getListTasksByAssignee(String listId, List<String> statuses, String assigneeUserId) {
        List<Map<String, Object>> all = new ArrayList<>();
        if (listId == null || listId.trim().isEmpty()
                || assigneeUserId == null || assigneeUserId.trim().isEmpty()) {
            return all;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int page = 0; page < 50; page++) {
            try {
                StringBuilder query = new StringBuilder();
                query.append("page=").append(page)
                        .append("&include_closed=true")
                        .append("&subtasks=true")
                        .append("&assignees%5B%5D=").append(UriUtils.encodeQueryParam(assigneeUserId, StandardCharsets.UTF_8));
                if (statuses != null) {
                    for (String s : statuses) {
                        query.append("&statuses%5B%5D=").append(UriUtils.encodeQueryParam(s, StandardCharsets.UTF_8));
                    }
                }

                URI uri = URI.create(CLICKUP_API_BASE + "/list/" + listId + "/task?" + query);

                ResponseEntity<Map> response = clickUpRestTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    break;
                }
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) body.get("tasks");
                if (tasks == null || tasks.isEmpty()) {
                    break;
                }
                all.addAll(tasks);

                Object lastPage = body.get("last_page");
                if (Boolean.TRUE.equals(lastPage) || tasks.size() < 100) {
                    break;
                }
            } catch (Exception e) {
                log.error("Erro ao buscar tarefas por assignee na list {} (page {}): {}", listId, page, e.getMessage());
                break;
            }
        }

        return all;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCurrentUser() {
        String token = parameterHelper.getClickUpToken();
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        // Cache por token — chamada única por instância pra cada token
        Map<String, Object> cached = currentUserCache.get(token);
        if (cached != null) {
            return cached;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            URI uri = URI.create(CLICKUP_API_BASE + "/user");
            ResponseEntity<Map> response = clickUpRestTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            Object userObj = body.get("user");
            if (!(userObj instanceof Map)) return null;

            Map<String, Object> user = (Map<String, Object>) userObj;
            currentUserCache.put(token, user);
            return user;
        } catch (Exception e) {
            log.warn("Falha ao buscar usuario atual do ClickUp (token invalido?): {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateTaskCustomField(String taskId, String fieldId, String value) {
        if (taskId == null || taskId.isBlank() || fieldId == null || fieldId.isBlank()) {
            return false;
        }
        String url = String.format("%s/task/%s/field/%s", CLICKUP_API_BASE, taskId, fieldId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("value", value == null ? "" : value);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = clickUpRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            }
            log.warn("Resposta inesperada ao atualizar custom field {} da task {}: {}", fieldId, taskId, response.getStatusCode());
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Task ou field nao encontrado no ClickUp: task={}, field={}", taskId, fieldId);
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP ao atualizar custom field {} da task {} - Status: {} - Body: {}",
                    fieldId, taskId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar custom field {} da task {}: {}", fieldId, taskId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateTaskDescription(String taskId, String description) {
        if (taskId == null || taskId.isBlank()) {
            return false;
        }
        String url = String.format("%s/task/%s", CLICKUP_API_BASE, taskId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", parameterHelper.getClickUpToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ClickUp tem 2 campos: 'description' (texto puro) e 'markdown_description'
        // (renderizado como rich text). Quando setamos 'markdown_description', os
        // comentários HTML (<!-- -->) usados como marcadores idempotentes ficam
        // INVISÍVEIS na UI. Setamos só esse campo — ClickUp deriva o 'description'
        // automaticamente removendo a formatação.
        Map<String, Object> body = new HashMap<>();
        body.put("markdown_description", description == null ? "" : description);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = clickUpRestTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            }
            log.warn("Resposta inesperada ao atualizar descricao da task {}: {}", taskId, response.getStatusCode());
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Task nao encontrada no ClickUp ao atualizar descricao: {}", taskId);
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP ao atualizar descricao da task {} - Status: {} - Body: {}",
                    taskId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar descricao da task {}: {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "ClickUp";
    }
}
