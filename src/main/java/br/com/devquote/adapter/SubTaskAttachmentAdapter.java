package br.com.devquote.adapter;

import br.com.devquote.dto.response.SubTaskAttachmentResponse;
import br.com.devquote.entity.SubTaskAttachment;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubTaskAttachmentAdapter {

    private final FileStorageStrategy fileStorageStrategy;

    public SubTaskAttachmentResponse toResponse(SubTaskAttachment entity) {
        if (entity == null) {
            return null;
        }

        return SubTaskAttachmentResponse.builder()
                .id(entity.getId())
                .subTaskId(entity.getSubTask() != null ? entity.getSubTask().getId() : null)
                .taskId(entity.getSubTask() != null && entity.getSubTask().getTask() != null
                        ? entity.getSubTask().getTask().getId() : null)
                .fileName(entity.getFileName())
                .originalFileName(entity.getOriginalFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .fileUrl(generateFileUrl(entity.getFilePath()))
                .uploadedAt(entity.getUploadedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<SubTaskAttachmentResponse> toResponseList(List<SubTaskAttachment> entities) {
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
