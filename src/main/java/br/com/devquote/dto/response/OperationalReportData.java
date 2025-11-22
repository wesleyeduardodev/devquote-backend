package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalReportData {

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private LocalDateTime dataGeracao;

    private String filtrosTipos;

    private String filtrosAmbientes;

    private String logoPath;

    private List<OperationalReportRow> linhas;

    private Long totalProducao;

    private Long totalHomologacao;

    private Long totalDesenvolvimento;

    private Long totalNaoEspecificado;

    private Long totalGeral;

    private OperationalReportStatistics statistics;

    @Builder.Default
    private String desenvolvedorNome = "Wesley Eduardo";

    @Builder.Default
    private String desenvolvedorTitulo = "Desenvolvedor Full Stack";

    @Builder.Default
    private String desenvolvedorEmail = "wesleyeduardo.dev@gmail.com";

    @Builder.Default
    private String desenvolvedorTelefone = "+55 98 98165-0805";

    @Builder.Default
    private String copyright = "© 2025 DevQuote. Todos os direitos reservados.";

    @Builder.Default
    private String sistemaTagline = "Sistema de Gestão de Orçamentos";
}
