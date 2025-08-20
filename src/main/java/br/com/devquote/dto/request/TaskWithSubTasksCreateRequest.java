package br.com.devquote.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskWithSubTasksCreateRequest {

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Code is required")
    private String code;

    private String link;

    @Size(max = 500, message = "meetingLink must be at most 500 characters")
    private String meetingLink;

    @Size(max = 256, message = "notes must be at most 256 characters")
    private String notes;

    @Valid
    @NotNull(message = "Subtasks are required")
    private List<@Valid SubTaskRequest> subTasks;

    @Builder.Default
    private Boolean createQuote = Boolean.FALSE;

    @Builder.Default
    private Boolean linkQuoteToBilling = Boolean.FALSE;

    private List<Long> projectsIds;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public BigDecimal getTotalAmount() {
        if (subTasks == null || subTasks.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = subTasks.stream()
                .filter(Objects::nonNull)
                .map(SubTaskRequest::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
