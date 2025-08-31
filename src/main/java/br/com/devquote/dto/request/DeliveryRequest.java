package br.com.devquote.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @Size(max = 200, message = "Branch must be at most 200 characters")
    private String branch;

    @Size(max = 300, message = "Pull request URL must be at most 300 characters")
    private String pullRequest;

    private String script;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;

    @Size(max = 256, message = "notes must be at most 256 characters")
    private String notes;

    @Size(max = 200, message = "sourceBranch must be at most 256 characters")
    private String sourceBranch;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate finishedAt;}
