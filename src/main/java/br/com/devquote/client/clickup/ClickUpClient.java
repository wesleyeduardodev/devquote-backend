package br.com.devquote.client.clickup;

import java.util.List;
import java.util.Map;

public interface ClickUpClient {

    boolean updateTaskStatus(String taskId, String status);

    Map<String, Object> getTask(String taskId);

    Map<String, Object> getList(String listId);

    List<String> getAvailableStatuses(String taskId);

    String getProviderName();
}
