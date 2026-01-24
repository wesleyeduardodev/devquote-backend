package br.com.devquote.minicurso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuloEventoResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private Integer ordem;
    private Integer cargaHoraria;
    private String cargaHorariaFormatada;
    private Boolean ativo;
    private List<ItemModuloResponse> itens;
    private List<InstrutorSimplificadoResponse> instrutores;
}
