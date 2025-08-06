package br.com.devquote.dto.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeasurementRequestDTO {

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be at least 1")
    @Max(value = 12, message = "Month must be at most 12")
    private Integer month;

    @NotNull(message = "Year is required")
    private Integer year;

    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDateTime paymentDate;

    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;
}