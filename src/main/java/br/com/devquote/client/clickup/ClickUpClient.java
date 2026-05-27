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

    /**
     * Atualiza o valor de um custom field de uma task.
     * Endpoint: POST /task/{taskId}/field/{fieldId} com body {"value": "..."}
     * Retorna true se 2xx.
     */
    boolean updateTaskCustomField(String taskId, String fieldId, String value);

    /**
     * Cria um comentário na task. Usa `notify_all=false` para não enviar
     * notificação a todos os assignees.
     * Endpoint: POST /task/{taskId}/comment
     * Retorna o `id` do comment criado (ou null em caso de erro).
     */
    String createTaskComment(String taskId, String commentText);

    String getProviderName();
}
