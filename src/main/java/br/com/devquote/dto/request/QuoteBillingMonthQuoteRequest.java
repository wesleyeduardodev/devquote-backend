package br.com.devquote.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuoteBillingMonthQuoteRequest {

    @NotNull(message = "quoteBillingMonthId is required")
    private Long quoteBillingMonthId;

    @NotNull(message = "quoteId is required")
    private Long quoteId;
}