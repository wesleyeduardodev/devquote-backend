package br.com.devquote.minicurso.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracaoEventoRequest {

    @NotBlank(message = "Titulo e obrigatorio")
    @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
    private String titulo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataEvento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;

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
