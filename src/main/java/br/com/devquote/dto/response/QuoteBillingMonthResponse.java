package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuoteBillingMonthResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private LocalDate paymentDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
