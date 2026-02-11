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
public class ConfiguracaoEventoResponse {

    private Long id;
    private String titulo;
    private List<DataEventoResponse> datasEvento;
    private String local;
    private Integer quantidadeVagas;
    private Boolean inscricoesAbertas;
    private Boolean exibirFaleConosco;
    private String emailContato;
    private String whatsappContato;
    private Integer vagasDisponiveis;
    private Long totalListaEspera;
    private Integer cargaHorariaTotal;
    private String cargaHorariaTotalFormatada;
    private List<ModuloEventoResponse> modulos;
}
