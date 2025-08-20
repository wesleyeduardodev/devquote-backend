package br.com.devquote.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskWithSubTasksResponse {

    private Long id;

    private Long requesterId;

    private String title;

    private String description;

    private String status;

    private String code;

    private String link;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<SubTaskResponse> subTasks;
}
