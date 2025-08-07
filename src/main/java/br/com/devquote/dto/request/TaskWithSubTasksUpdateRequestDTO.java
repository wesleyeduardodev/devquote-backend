package br.com.devquote.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskWithSubTasksUpdateRequestDTO {

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
    private List<@Valid SubTaskUpdateRequestDTO> subTasks;
}
