package br.com.devquote.client.board;

import br.com.devquote.enums.BoardFilterMode;

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

    /** Busca as tarefas de prioridade do dono no modo padrão (Desenvolvedor E Responsável). */
    List<BoardTask> fetchPriorityTasks();

    /**
     * Busca as tarefas do board conforme o {@link BoardFilterMode} (partição exclusiva
     * por Desenvolvedor × Responsável). Implementações que não conhecem o conceito de
     * assignee podem ignorar o modo e cair no comportamento padrão.
     */
    default List<BoardTask> fetchPriorityTasks(BoardFilterMode mode) {
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
