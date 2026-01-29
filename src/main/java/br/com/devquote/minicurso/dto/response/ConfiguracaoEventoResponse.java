package br.com.devquote.minicurso.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracaoEventoResponse {

    private Long id;
    private String titulo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataEvento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime horarioFim;

    private String local;
    private Integer quantidadeVagas;
    private Boolean inscricoesAbertas;
    private Boolean exibirFaleConosco;
    private String emailContato;
    private String whatsappContato;
    private Integer vagasDisponiveis;
    private Integer cargaHorariaTotal;
    private String cargaHorariaTotalFormatada;
    private List<ModuloEventoResponse> modulos;
}
