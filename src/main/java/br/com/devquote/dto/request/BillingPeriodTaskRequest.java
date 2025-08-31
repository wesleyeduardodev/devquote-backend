package br.com.devquote.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingPeriodTaskRequest {
    
    @NotNull(message = "ID do período de faturamento é obrigatório")
    private Long billingPeriodId;
    
    @NotNull(message = "ID da tarefa é obrigatório")
    private Long taskId;
}