package br.com.devquote.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskWithSubTasksRequestDTO {

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

    @Valid
    @NotNull(message = "Subtasks are required")
    private List<@Valid SubTaskRequestDTO> subTasks;
}
