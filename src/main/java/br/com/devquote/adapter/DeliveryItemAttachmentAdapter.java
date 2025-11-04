package br.com.devquote.adapter;
import br.com.devquote.dto.response.DeliveryItemAttachmentResponse;
import br.com.devquote.entity.DeliveryItemAttachment;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeliveryItemAttachmentAdapter {

    public DeliveryItemAttachmentResponse toResponse(DeliveryItemAttachment entity) {
        if (entity == null) {
            return null;
        }
        
        return DeliveryItemAttachmentResponse.builder()
                .id(entity.getId())
                .deliveryItemId(entity.getDeliveryItem().getId())
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .uploadedAt(entity.getUploadedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<DeliveryItemAttachmentResponse> toResponseList(List<DeliveryItemAttachment> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}