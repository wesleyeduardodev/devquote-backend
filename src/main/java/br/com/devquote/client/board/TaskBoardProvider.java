package br.com.devquote.client.board;

import java.util.List;
import java.util.Map;

/**
 * Provedor de board de tarefas (ClickUp hoje; Jira/outros no futuro).
 * Implementações são auto-registradas como beans e selecionadas pelo
 * {@link TaskBoardProviderFactory} via parâmetro TASK_BOARD_PROVIDER.
 */
public interface TaskBoardProvider {

    /** Chave do provider (ex.: "CLICKUP", "JIRA"). */
    String getProviderName();

    /** Indica se este provider atende a chave configurada. */
    boolean supports(String providerKey);

    /** True se há config suficiente (token + ids) para consultar o board. */
    boolean isConfigured();

    /** Busca as tarefas de prioridade do dono (custom field Desenvolvedor). */
    List<BoardTask> fetchPriorityTasks();

    /**
     * Busca as tarefas de prioridade do dono, opcionalmente incluindo tarefas
     * onde o dono é o Responsável (assignee). Implementações que não conhecem
     * o conceito de assignee podem ignorar o flag.
     */
    default List<BoardTask> fetchPriorityTasks(boolean includeAssignee) {
        return fetchPriorityTasks();
    }

    /**
     * Retorna info do user conectado (dono do token), pra a UI mostrar feedback.
     * Default null pra providers que não conhecem essa noção.
     */
    default Map<String, Object> getCurrentUser() {
        return null;
    }
}
