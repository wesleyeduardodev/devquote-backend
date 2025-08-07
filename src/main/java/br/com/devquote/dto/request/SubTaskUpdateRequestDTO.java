package br.com.devquote.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubTaskUpdateRequestDTO {

    private Long id;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal amount;

    @NotBlank(message = "Status is required")
    private String status;

    private boolean excluded;
}
