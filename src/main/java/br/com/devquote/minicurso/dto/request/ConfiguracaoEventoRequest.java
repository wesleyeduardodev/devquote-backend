package br.com.devquote.minicurso.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracaoEventoRequest {

    @NotBlank(message = "Titulo e obrigatorio")
    @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
    private String titulo;

    @Valid
    private List<DataEventoRequest> datasEvento;

    @Size(max = 200, message = "Local deve ter no maximo 200 caracteres")
    private String local;

    private Integer quantidadeVagas;

    private Boolean inscricoesAbertas;

    private Boolean exibirFaleConosco;

    @Size(max = 150, message = "Email de contato deve ter no máximo 150 caracteres")
    private String emailContato;

    @Size(max = 20, message = "WhatsApp de contato deve ter no máximo 20 caracteres")
    private String whatsappContato;
}
