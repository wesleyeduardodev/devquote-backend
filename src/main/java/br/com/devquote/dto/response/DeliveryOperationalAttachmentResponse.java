package br.com.devquote.dto.response;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryOperationalAttachmentResponse {

    private Long id;
    private Long deliveryOperationalItemId;
    private String fileName;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
