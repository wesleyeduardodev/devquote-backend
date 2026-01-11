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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpClientImpl implements ClickUpClient {

    private static final String CLICKUP_API_BASE = "https://api.clickup.com/api/v2";

    private final RestTemplate clickUpRestTemplate;
    private final ClickUpParameterHelper parameterHelper;

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
    public String getProviderName() {
        return "ClickUp";
    }
}
