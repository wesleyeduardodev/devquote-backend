package br.com.devquote.dto.response;
import lombok.*;
import java.math.BigDecimal;
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


    private String code;

    private String link;

    private String meetingLink;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdByUserId;

    private String createdByUserName;

    private Long updatedByUserId;

    private String updatedByUserName;

    private Boolean hasSubTasks;

    private BigDecimal amount;

    private String taskType;

    private String serverOrigin;

    private String systemModule;

    private String priority;

    private List<SubTaskResponse> subTasks;
}
