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

    private static final String DEFAULT_STATUSES =
            "a iniciar - dev interno,em progresso,desenvolvimento concluído,pronto para testes,testes concluídos,validação em produção";

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

    public String getPrimaryStatus() {
        return systemParameterService.getString("CLICKUP_PRIMARY_STATUS", "a iniciar - dev interno");
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
}
