package br.com.devquote.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;

    private String notes;

    private List<DeliveryItemRequest> items;
}
