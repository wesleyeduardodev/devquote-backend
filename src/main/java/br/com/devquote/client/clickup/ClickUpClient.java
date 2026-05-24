package br.com.devquote.client.clickup;

import java.util.List;
import java.util.Map;

public interface ClickUpClient {

    boolean updateTaskStatus(String taskId, String status);

    Map<String, Object> getTask(String taskId);

    String getTaskStatus(String taskId);

    Map<String, Object> getList(String listId);

    List<String> getAvailableStatuses(String taskId);

    /**
     * Busca tarefas de uma list filtrando por status e (opcionalmente) por um custom field
     * drop_down (ex.: Desenvolvedor = opção). Pagina internamente. Retorna os mapas crus.
     */
    List<Map<String, Object>> getListTasksFiltered(String listId, List<String> statuses, String devFieldId, String devOptionId);

    /**
     * Busca tarefas de uma list filtrando por status e (opcionalmente) por responsável (assignee userId).
     * Pagina internamente. Retorna os mapas crus.
     */
    List<Map<String, Object>> getListTasksByAssignee(String listId, List<String> statuses, String assigneeUserId);

    /**
     * Retorna o user dono do token configurado (id, username, email, etc.).
     * Cacheado em memória — chamada única por instância (o user do token não muda em runtime).
     * Retorna null se a chamada falhar (token inválido, sem rede).
     */
    Map<String, Object> getCurrentUser();

    String getProviderName();
}
