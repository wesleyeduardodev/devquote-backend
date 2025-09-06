package br.com.devquote.adapter;

import br.com.devquote.dto.response.TaskAttachmentResponse;
import br.com.devquote.entity.TaskAttachment;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskAttachmentAdapter {

    private final FileStorageStrategy fileStorageStrategy;

    public TaskAttachmentResponse toResponse(TaskAttachment entity) {
        if (entity == null) {
            return null;
        }

        return TaskAttachmentResponse.builder()
                .id(entity.getId())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
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

    public List<TaskAttachmentResponse> toResponseList(List<TaskAttachment> entities) {
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
            // Log error but don't fail the entire response
            return null;
        }
    }
}