package br.com.devquote.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubTaskUpdateRequest {

    private Long id;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @DecimalMin(value = "0.00", message = "Amount must be greater than or equal to zero")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal amount;


    private boolean excluded;
}
