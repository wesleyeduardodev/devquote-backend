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
public class MeasurementResponseDTO {

    private Long id;

    private Integer month;

    private Integer year;

    private LocalDateTime paymentDate;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}