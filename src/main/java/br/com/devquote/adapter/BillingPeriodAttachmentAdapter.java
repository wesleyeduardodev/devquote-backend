package br.com.devquote.adapter;
import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import br.com.devquote.entity.BillingPeriodAttachment;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BillingPeriodAttachmentAdapter {

    private final FileStorageStrategy fileStorageStrategy;

    public BillingPeriodAttachmentResponse toResponse(BillingPeriodAttachment entity) {
        if (entity == null) {
            return null;
        }

        return BillingPeriodAttachmentResponse.builder()
                .id(entity.getId())
                .billingPeriodId(entity.getBillingPeriod() != null ? entity.getBillingPeriod().getId() : null)
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

    public List<BillingPeriodAttachmentResponse> toResponseList(List<BillingPeriodAttachment> entities) {
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