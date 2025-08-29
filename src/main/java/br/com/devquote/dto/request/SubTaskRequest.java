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
public class SubTaskRequest {

    private Long taskId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;

    @DecimalMin(value = "0.00", message = "Amount must be greater than or equal to zero")
    @Digits(integer = 8, fraction = 2, message = "Amount must be a valid monetary value with up to 8 digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;
}