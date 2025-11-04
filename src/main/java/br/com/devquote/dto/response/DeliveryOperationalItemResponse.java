package br.com.devquote.dto.response;
import lombok.*;
import java.time.LocalDate;
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
    private LocalDate startedAt;
    private LocalDate finishedAt;
    private List<DeliveryOperationalAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
