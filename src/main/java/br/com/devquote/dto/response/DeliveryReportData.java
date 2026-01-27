package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryReportData {

    private Long id;

    private Long taskId;

    private String taskCode;

    private String taskTitle;

    private List<ContentBlock> taskDescriptionBlocks;

    private BigDecimal taskAmount;

    private String flowType;

    private String flowTypeLabel;

    private String environment;

    private String environmentLabel;

    private String status;

    private String statusLabel;

    private String notes;

    private List<ContentBlock> notesBlocks;

    private boolean hasNotesContent;

    private LocalDateTime startedAt;

    private String startedAtFormatted;

    private LocalDateTime finishedAt;

    private String finishedAtFormatted;

    private Integer totalItems;

    private List<DeliveryItemReportRow> items;

    private LocalDateTime createdAt;

    private String createdAtFormatted;

    private LocalDateTime updatedAt;

    private String updatedAtFormatted;

    private LocalDateTime dataGeracao;

    @Builder.Default
    private String desenvolvedorNome = "Wesley Eduardo";

    @Builder.Default
    private String desenvolvedorTitulo = "Desenvolvedor Full Stack";

    @Builder.Default
    private String desenvolvedorEmail = "wesleyeduardo.dev@gmail.com";

    @Builder.Default
    private String desenvolvedorTelefone = "98 98165-0805";

    @Builder.Default
    private String copyright = "© 2025 DevQuote. Todos os direitos reservados.";

    @Builder.Default
    private String sistemaTagline = "Gestao Inteligente de Projetos de Desenvolvimento";
}
