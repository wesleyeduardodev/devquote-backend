package br.com.devquote.dto.request;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingNoteRequest {

    /** Nulo = anotação geral de faturamento. Preenchido = anotação do período. */
    private Long billingPeriodId;

    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String title;

    private String content;
}
