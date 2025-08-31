package br.com.devquote.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryResponse {

    private Long id;
    private Long taskId;
    private String taskName;
    private String taskCode;
    private Long projectId;
    private String projectName;
    private String branch;
    private String pullRequest;
    private String script;
    private String status;
    private String notes;
    private String sourceBranch;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate finishedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
