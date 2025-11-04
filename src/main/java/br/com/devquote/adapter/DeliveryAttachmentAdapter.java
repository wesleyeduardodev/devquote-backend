package br.com.devquote.adapter;
import br.com.devquote.dto.response.DeliveryAttachmentResponse;
import br.com.devquote.entity.DeliveryAttachment;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeliveryAttachmentAdapter {

    public DeliveryAttachmentResponse toResponse(DeliveryAttachment entity) {
        if (entity == null) {
            return null;
        }
        
        return DeliveryAttachmentResponse.builder()
                .id(entity.getId())
                .deliveryId(entity.getDelivery().getId())
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .uploadedAt(entity.getUploadedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<DeliveryAttachmentResponse> toResponseList(List<DeliveryAttachment> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}