package br.com.devquote.enums;

/**
 * Modo de filtragem do board de tarefas (Tarefas ClickUp). Partição exclusiva pelos
 * dois papéis independentes do dono na tarefa: Desenvolvedor (custom field) e
 * Responsável (assignee). Cada tarefa cai em exatamente um modo.
 *
 * <ul>
 *   <li>{@link #DEV_NOT_ASSIGNEE} — sou Desenvolvedor e NÃO sou Responsável (dev \ assignee).</li>
 *   <li>{@link #DEV_AND_ASSIGNEE} — sou Desenvolvedor E Responsável (dev ∩ assignee). Padrão.</li>
 *   <li>{@link #ASSIGNEE_NOT_DEV} — sou Responsável e NÃO sou Desenvolvedor (assignee \ dev).</li>
 * </ul>
 */
public enum BoardFilterMode {
    DEV_NOT_ASSIGNEE,
    DEV_AND_ASSIGNEE,
    ASSIGNEE_NOT_DEV
}
