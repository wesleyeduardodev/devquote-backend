package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingPeriodResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private LocalDate paymentDate;
    private String status;
    private Boolean billingEmailSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount;
    private Long taskCount;
}
