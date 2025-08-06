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
public class MeasurementQuoteRequestDTO {

    @NotNull(message = "Measurement ID is required")
    private Long measurementId;

    @NotNull(message = "Quote ID is required")
    private Long quoteId;
}