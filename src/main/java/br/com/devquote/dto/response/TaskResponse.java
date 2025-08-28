package br.com.devquote.dto.response;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {

    private Long id;

    private Long requesterId;

    private String requesterName;

    private String title;

    private String description;

    private String status;

    private String code;

    private String link;

    private String meetingLink;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdByUserId;

    private String createdByUserName;

    private Long updatedByUserId;

    private String updatedByUserName;

    private List<SubTaskResponse> subTasks;
}
