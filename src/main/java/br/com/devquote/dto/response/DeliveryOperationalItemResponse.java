package br.com.devquote.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryOperationalItemResponse {

    private Long id;
    private Long deliveryId;
    private String title;
    private String description;
    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private List<DeliveryOperationalAttachmentResponse> attachments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
