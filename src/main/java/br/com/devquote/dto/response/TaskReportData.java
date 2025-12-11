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
public class TaskReportData {

    private Long id;

    private String code;

    private String title;

    private String description;

    private String flowType;

    private String flowTypeLabel;

    private String taskType;

    private String taskTypeLabel;

    private String environment;

    private String environmentLabel;

    private String priority;

    private String priorityLabel;

    private String systemModule;

    private String serverOrigin;

    private String link;

    private String meetingLink;

    private String requesterName;

    private String requesterPhone;

    private String requesterEmail;

    private Boolean hasDelivery;

    private String hasDeliveryLabel;

    private Boolean hasQuoteInBilling;

    private String hasQuoteInBillingLabel;

    private BigDecimal totalAmount;

    private String totalAmountFormatted;

    private Boolean showValues;

    private Integer subTasksCount;

    private List<SubTaskReportRow> subTasks;

    private BigDecimal subTasksTotalAmount;

    private String subTasksTotalAmountFormatted;

    private LocalDateTime createdAt;

    private String createdAtFormatted;

    private LocalDateTime updatedAt;

    private String updatedAtFormatted;

    private String createdByUserName;

    private String updatedByUserName;

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
