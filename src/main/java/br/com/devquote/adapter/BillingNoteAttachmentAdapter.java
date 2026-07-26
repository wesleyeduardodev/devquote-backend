package br.com.devquote.adapter;
import br.com.devquote.dto.response.BillingNoteAttachmentResponse;
import br.com.devquote.entity.BillingNoteAttachment;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BillingNoteAttachmentAdapter {

    private final FileStorageStrategy fileStorageStrategy;

    public BillingNoteAttachmentResponse toResponse(BillingNoteAttachment entity) {
        if (entity == null) {
            return null;
        }

        return BillingNoteAttachmentResponse.builder()
                .id(entity.getId())
                .billingNoteId(entity.getBillingNote() != null ? entity.getBillingNote().getId() : null)
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .fileUrl(generateFileUrl(entity.getFilePath()))
                .excluded(entity.getExcluded())
                .uploadedAt(entity.getUploadedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<BillingNoteAttachmentResponse> toResponseList(List<BillingNoteAttachment> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        try {
            return fileStorageStrategy.getFileUrl(filePath);
        } catch (Exception e) {
            return null;
        }
    }
}
