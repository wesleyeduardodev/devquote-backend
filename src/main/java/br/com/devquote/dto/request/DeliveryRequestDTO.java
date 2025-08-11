package br.com.devquote.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryRequestDTO {

    @NotNull(message = "Quote ID is required")
    private Long quoteId;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate finishedAt;}
