package br.com.devquote.dto.request;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryNotesRequest {

    @Size(max = 5000, message = "Observações não podem exceder 5000 caracteres")
    private String notes;
}
