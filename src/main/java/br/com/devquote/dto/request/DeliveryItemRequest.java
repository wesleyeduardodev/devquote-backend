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
public class DeliveryItemRequest {

    @NotNull(message = "Delivery ID is required")
    private Long deliveryId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status must be at most 30 characters")
    private String status;

    @Size(max = 200, message = "Branch must be at most 200 characters")
    private String branch;

    @Size(max = 200, message = "Source branch must be at most 200 characters")
    private String sourceBranch;

    @Size(max = 500, message = "Pull request URL must be at most 500 characters")
    private String pullRequest;


    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate finishedAt;
}