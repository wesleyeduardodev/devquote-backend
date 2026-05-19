package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAmountSumResponse {
    /** Soma do campo `amount` das tarefas que batem com os filtros aplicados. */
    private BigDecimal totalAmount;
}
