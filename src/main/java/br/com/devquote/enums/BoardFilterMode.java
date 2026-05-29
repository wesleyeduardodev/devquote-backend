package br.com.devquote.enums;

/**
 * Modo de filtragem do board de tarefas (Tarefas ClickUp), pelos dois papéis
 * independentes do dono na tarefa: Desenvolvedor (custom field) e Responsável (assignee).
 *
 * <ul>
 *   <li>{@link #DEV_OR_ASSIGNEE} — sou Desenvolvedor OU Responsável (união = tudo que me envolve). Padrão.</li>
 *   <li>{@link #DEV_NOT_ASSIGNEE} — sou Desenvolvedor e NÃO sou Responsável (dev \ assignee).</li>
 *   <li>{@link #DEV_AND_ASSIGNEE} — sou Desenvolvedor E Responsável (dev ∩ assignee).</li>
 *   <li>{@link #ASSIGNEE_NOT_DEV} — sou Responsável e NÃO sou Desenvolvedor (assignee \ dev).</li>
 * </ul>
 *
 * Os 3 últimos são uma partição exclusiva; o primeiro é a união deles.
 */
public enum BoardFilterMode {
    DEV_OR_ASSIGNEE,
    DEV_NOT_ASSIGNEE,
    DEV_AND_ASSIGNEE,
    ASSIGNEE_NOT_DEV
}
