package br.com.devquote.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;

    // Lista de itens da entrega
    private java.util.List<DeliveryItemRequest> items;
}
