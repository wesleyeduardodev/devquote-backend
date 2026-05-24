package br.com.devquote.dto.request;

import lombok.Data;

import java.util.List;

/**
 * Atualização parcial das preferências do board de prioridades.
 * Todos os campos são opcionais — somente o que for não-null é persistido.
 *
 *  - orderedStatuses: nova ordem dos grupos (CLICKUP_PRIORITY_STATUSES)
 *  - primaryStatus:   status com badge "Principal" (CLICKUP_PRIMARY_STATUS)
 *  - hiddenStatuses:  lista de status ocultos do board (CLICKUP_HIDDEN_STATUSES)
 */
@Data
public class BoardPreferencesRequest {
    private List<String> orderedStatuses;
    private String primaryStatus;
    private List<String> hiddenStatuses;
}
