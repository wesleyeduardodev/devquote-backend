package br.com.devquote.minicurso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstrutorSimplificadoResponse {

    private Long id;
    private String nome;
}
