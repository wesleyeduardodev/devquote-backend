package br.com.devquote.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    @Size(max = 300, message = "Link deve ter no máximo 300 caracteres")
    @Pattern(regexp = "^(http(s)?://.*)?$", message = "Link deve ser uma URL válida")
    private String link;
}
