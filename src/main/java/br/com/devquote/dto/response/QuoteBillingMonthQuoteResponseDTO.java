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
public class QuoteBillingMonthQuoteResponseDTO {
    private Long id;
    private Long quoteBillingMonthId;
    private Long quoteId;
    private String taskName;
    private String taskCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}