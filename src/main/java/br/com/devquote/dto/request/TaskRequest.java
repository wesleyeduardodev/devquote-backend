package br.com.devquote.dto.request;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRequest {

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;

    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code must be at most 100 characters")
    private String code;

    @Size(max = 500, message = "meetingLink must be at most 500 characters")
    private String meetingLink;

    @Size(max = 256, message = "notes must be at most 256 characters")
    private String notes;

    @Size(max = 200, message = "Link must be at most 200 characters")
    @Pattern(regexp = "^(http(s)?://.*)?$", message = "Link must be a valid URL")
    private String link;

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
}