package br.com.devquote.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskWithSubTasksUpdateRequest {

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;


    @NotBlank(message = "Code is required")
    private String code;

    private String link;

    @Size(max = 500, message = "meetingLink must be at most 500 characters")
    private String meetingLink;

    @Size(max = 256, message = "notes must be at most 256 characters")
    private String notes;

    @Builder.Default
    private Boolean createQuote = Boolean.FALSE;

    @Builder.Default
    private Boolean linkQuoteToBilling = Boolean.FALSE;

    @Builder.Default
    private Boolean hasSubTasks = false;

    @DecimalMin(value = "0.0", message = "Amount must be greater than or equal to 0", groups = {})
    private BigDecimal amount;

    @Size(max = 50, message = "Task type must be at most 50 characters")
    private String taskType;

    @Size(max = 100, message = "Server origin must be at most 100 characters")
    private String serverOrigin;

    @Size(max = 100, message = "System module must be at most 100 characters")
    private String systemModule;

    @Size(max = 20, message = "Priority must be at most 20 characters")
    @Builder.Default
    private String priority = "MEDIUM";

    @Valid
    private List<@Valid SubTaskUpdateRequest> subTasks;
}
