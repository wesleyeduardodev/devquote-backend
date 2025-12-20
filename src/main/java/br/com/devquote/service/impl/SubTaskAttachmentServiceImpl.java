package br.com.devquote.service.impl;

import br.com.devquote.adapter.SubTaskAttachmentAdapter;
import br.com.devquote.dto.response.SubTaskAttachmentResponse;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.SubTaskAttachment;
import br.com.devquote.repository.SubTaskAttachmentRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.service.SubTaskAttachmentService;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubTaskAttachmentServiceImpl implements SubTaskAttachmentService {

    private final SubTaskAttachmentRepository subTaskAttachmentRepository;
    private final SubTaskRepository subTaskRepository;
    private final SubTaskAttachmentAdapter subTaskAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<SubTaskAttachmentResponse> uploadFiles(Long subTaskId, List<MultipartFile> files) {
        List<SubTaskAttachmentResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                SubTaskAttachmentResponse response = uploadFile(subTaskId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for subtask {}: {}", file.getOriginalFilename(), subTaskId, e.getMessage());
            }
        }
        return responses;
    }

    @Override
    public SubTaskAttachmentResponse uploadFile(Long subTaskId, MultipartFile file) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("Subtarefa nao encontrada com ID: " + subTaskId));

        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Long taskId = subTask.getTask().getId();
            String filePath = buildFilePath(taskId, subTaskId, fileName);

            String uploadedFilePath = fileStorageStrategy.uploadFile(file, filePath);

            SubTaskAttachment attachment = SubTaskAttachment.builder()
                    .subTask(subTask)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(uploadedFilePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            SubTaskAttachment savedAttachment = subTaskAttachmentRepository.save(attachment);

            log.info("File uploaded successfully: {} for subtask {}", fileName, subTaskId);
            return subTaskAttachmentAdapter.toResponse(savedAttachment);

        } catch (IOException e) {
            log.error("Error uploading file for subtask {}: {}", subTaskId, e.getMessage());
            throw new RuntimeException("Falha ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubTaskAttachmentResponse> getSubTaskAttachments(Long subTaskId) {
        List<SubTaskAttachment> attachments = subTaskAttachmentRepository.findBySubTaskId(subTaskId);
        return subTaskAttachmentAdapter.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubTaskAttachment> getSubTaskAttachmentsEntities(Long subTaskId) {
        return subTaskAttachmentRepository.findBySubTaskId(subTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubTaskAttachmentResponse getAttachmentById(Long attachmentId) {
        SubTaskAttachment attachment = subTaskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo nao encontrado com ID: " + attachmentId));

        return subTaskAttachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        SubTaskAttachment attachment = subTaskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo nao encontrado com ID: " + attachmentId));

        try {
            return new InputStreamResource(fileStorageStrategy.getFileStream(attachment.getFilePath()));
        } catch (IOException e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            throw new RuntimeException("Falha ao baixar anexo: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        SubTaskAttachment attachment = subTaskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo nao encontrado com ID: " + attachmentId));

        subTaskAttachmentRepository.delete(attachment);
        log.info("Attachment deleted from database: {}", attachment.getOriginalFileName());

        try {
            fileStorageStrategy.deleteFile(attachment.getFilePath());
            log.info("File deleted from storage: {}", attachment.getFilePath());
        } catch (Exception e) {
            log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
        }
    }

    @Override
    public void deleteAttachments(List<Long> attachmentIds) {
        for (Long attachmentId : attachmentIds) {
            try {
                deleteAttachment(attachmentId);
            } catch (Exception e) {
                log.error("Error deleting attachment {}: {}", attachmentId, e.getMessage());
            }
        }
    }

    @Override
    public void deleteAllSubTaskAttachments(Long subTaskId) {
        List<SubTaskAttachment> attachments = subTaskAttachmentRepository.findBySubTaskId(subTaskId);

        for (SubTaskAttachment attachment : attachments) {
            subTaskAttachmentRepository.delete(attachment);

            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
                log.info("File deleted from storage: {}", attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
        }

        log.info("Deleted {} attachments from database for subtask {}", attachments.size(), subTaskId);
    }

    @Override
    public void deleteAllSubTaskAttachmentsAndFolder(Long subTaskId) {
        log.info("Physically deleting all attachments and folder for subtask ID: {}", subTaskId);

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new RuntimeException("Subtarefa nao encontrada com ID: " + subTaskId));

        List<SubTaskAttachment> attachments = subTaskAttachmentRepository.findBySubTaskId(subTaskId);

        if (!attachments.isEmpty()) {
            subTaskAttachmentRepository.deleteAll(attachments);
            log.info("Physically deleted {} attachments from database for subtask {}", attachments.size(), subTaskId);
        }

        Long taskId = subTask.getTask().getId();
        String folderPath = "tasks/" + taskId + "/subtasks/" + subTaskId + "/";
        try {
            boolean deleted = fileStorageStrategy.deleteFolder(folderPath);
            if (deleted) {
                log.info("Successfully deleted folder from storage: {}", folderPath);
            } else {
                log.warn("Failed to delete folder from storage: {}", folderPath);
            }
        } catch (Exception e) {
            log.error("Error deleting folder from storage: {} - {}", folderPath, e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo nao pode estar vazio");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Arquivo muito grande. Tamanho maximo: 10MB");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || contentType.isEmpty() || contentType.equals("application/octet-stream") || !isAllowedContentType(contentType)) {
            if (isAllowedByExtension(originalFilename)) {
                return;
            }
            throw new RuntimeException("Tipo de arquivo nao permitido: " + contentType + " (arquivo: " + originalFilename + ")");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        List<String> allowedTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            "application/json",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "video/mp4",
            "video/avi",
            "video/quicktime",
            "video/x-msvideo",
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed"
        );

        return allowedTypes.contains(contentType);
    }

    private boolean isAllowedByExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "json",
            "jpg", "jpeg", "png", "gif", "webp",
            "mp4", "avi", "mov", "wmv",
            "zip", "rar", "7z"
        );

        return allowedExtensions.contains(extension);
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private String buildFilePath(Long taskId, Long subTaskId, String fileName) {
        return String.format("tasks/%d/subtasks/%d/attachments/%s", taskId, subTaskId, fileName);
    }
}
