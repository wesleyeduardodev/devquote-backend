package br.com.devquote.dto.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuoteRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", message = "Total Amount must be greater than or equal to zero")
    @Digits(integer = 8, fraction = 2, message = "Total amount must be a valid monetary value with up to 8 digits and 2 decimal places")
    private BigDecimal totalAmount;
}