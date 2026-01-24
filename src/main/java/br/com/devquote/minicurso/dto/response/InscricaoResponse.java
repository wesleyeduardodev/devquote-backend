package br.com.devquote.minicurso.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InscricaoResponse {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String curso;
    private String periodo;
    private String nivelProgramacao;
    private String expectativa;
    private Boolean confirmado;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
