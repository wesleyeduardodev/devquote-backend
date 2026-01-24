package br.com.devquote.minicurso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemModuloResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private Integer ordem;
    private Integer duracao;
    private Boolean ativo;
}
