package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatsResponse {
    /** Total de tarefas (todas). */
    private long total;
    /** Tarefas sem qualquer entrega vinculada. */
    private long totalWithoutDelivery;
    /** Tarefas sem qualquer vínculo a período de faturamento. */
    private long totalWithoutBilling;
}
