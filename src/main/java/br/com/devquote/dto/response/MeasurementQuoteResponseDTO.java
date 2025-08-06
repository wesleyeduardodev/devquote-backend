package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeasurementQuoteResponseDTO {

    private Long id;

    private Long measurementId;

    private Long quoteId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}