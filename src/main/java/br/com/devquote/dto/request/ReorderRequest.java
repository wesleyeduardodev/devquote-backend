package br.com.devquote.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReorderRequest {

    @NotNull(message = "Delivery ID is required")
    private Long deliveryId;

    private List<ReorderItemRequest> items;
}
