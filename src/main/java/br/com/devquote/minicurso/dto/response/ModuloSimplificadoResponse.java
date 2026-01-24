package br.com.devquote.minicurso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuloSimplificadoResponse {

    private Long id;
    private String titulo;
}
