package br.com.devquote.helper;

import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Config (em system_parameter, editável na tela /parameters) do board de prioridades.
 * Tudo lido com default vazio → se faltar, o board vem "não configurado".
 */
@Component
@RequiredArgsConstructor
public class TaskBoardParameterHelper {

    // Ordem do board (Opção A — primary no topo, auxiliares no fim).
    // Agora serve apenas como "ordem preferencial": a busca no ClickUp
    // não filtra por status, traz tudo que tem o developer. Status que
    // não estão aqui caem no fim do board, na ordem que aparecerem.
    private static final String DEFAULT_STATUSES =
            "a iniciar - dev interno,em progresso,desenvolvimento concluído,pronto para testes,testes concluídos,validação em produção,em análise - suporte,backlog";

    // Default: NENHUM status oculto — mostra tudo o que vier do ClickUp.
    // O user esconde o que não quiser ver via botão 👁️ no /priorities
    // (1 click cria/atualiza o CLICKUP_HIDDEN_STATUSES no banco).
    private static final String DEFAULT_HIDDEN_STATUSES = "";

    private final SystemParameterService systemParameterService;

    public String getProvider() {
        return systemParameterService.getString("TASK_BOARD_PROVIDER", "CLICKUP");
    }

    public String getClickUpListId() {
        return systemParameterService.getString("CLICKUP_BOARD_LIST_ID", "");
    }

    public String getClickUpDeveloperFieldId() {
        return systemParameterService.getString("CLICKUP_DEVELOPER_FIELD_ID", "");
    }

    public String getClickUpDeveloperOptionId() {
        return systemParameterService.getString("CLICKUP_DEVELOPER_OPTION_ID", "");
    }

    public String getClickUpOrderFieldId() {
        return systemParameterService.getString("CLICKUP_ORDER_FIELD_ID", "");
    }

    /** ID do custom field "Branch" no ClickUp. Quando preenchido, o sync de Delivery
     *  propaga os PRs dos items pra esse campo + descrição da task. Vazio = pula. */
    public String getClickUpBranchFieldId() {
        return systemParameterService.getString("CLICKUP_BRANCH_FIELD_ID", "");
    }

    public String getPrimaryStatus() {
        return systemParameterService.getString("CLICKUP_PRIMARY_STATUS", "a iniciar - dev interno");
    }

    /** User ID numérico do ClickUp pra filtrar tarefas onde o user é Responsável (assignee).
     *  Quando vazio, o board não combina por responsável — só pelo custom field Developer. */
    public String getBoardAssigneeUserId() {
        return systemParameterService.getString("CLICKUP_BOARD_ASSIGNEE_USER_ID", "");
    }

    public List<String> getPriorityStatuses() {
        String csv = systemParameterService.getString("CLICKUP_PRIORITY_STATUSES", DEFAULT_STATUSES);
        if (csv == null || csv.trim().isEmpty()) {
            csv = DEFAULT_STATUSES;
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Status ocultados do board (lower-case, comparados case-insensitive).
     *
     * Regras:
     *  - Parâmetro NÃO cadastrado → usa default ({@link #DEFAULT_HIDDEN_STATUSES}).
     *  - Parâmetro cadastrado com valor VAZIO → não oculta nada (mostra TUDO).
     *  - Parâmetro com valor → usa o CSV configurado.
     */
    public List<String> getHiddenStatuses() {
        String csv;
        try {
            csv = systemParameterService.getString("CLICKUP_HIDDEN_STATUSES");
        } catch (Exception e) {
            // Não cadastrado → default
            csv = DEFAULT_HIDDEN_STATUSES;
        }
        if (csv == null) csv = "";  // valor null no banco trata igual a vazio
        if (csv.trim().isEmpty()) {
            // Cadastrado vazio → lista vazia → não oculta nada
            return new java.util.ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
