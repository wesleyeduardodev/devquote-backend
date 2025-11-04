package br.com.devquote.adapter;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.entity.DeliveryOperationalAttachment;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeliveryOperationalAttachmentAdapter {

    public DeliveryOperationalAttachmentResponse toResponse(DeliveryOperationalAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return DeliveryOperationalAttachmentResponse.builder()
                .id(attachment.getId())
                .deliveryOperationalItemId(attachment.getDeliveryOperationalItem().getId())
                .fileName(attachment.getFileName())
                .originalName(attachment.getOriginalName())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .uploadedAt(attachment.getUploadedAt())
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                .build();
    }

    public List<DeliveryOperationalAttachmentResponse> toResponseList(List<DeliveryOperationalAttachment> attachments) {
        return attachments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
