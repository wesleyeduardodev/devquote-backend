package br.com.devquote.client.board;

import java.util.List;

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

    /** Busca as tarefas de prioridade do dono, já filtradas pelos status configurados. */
    List<BoardTask> fetchPriorityTasks();
}
