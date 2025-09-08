package br.com.devquote.service.impl;

import br.com.devquote.adapter.TaskAttachmentAdapter;
import br.com.devquote.dto.response.TaskAttachmentResponse;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;
import br.com.devquote.repository.TaskAttachmentRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.TaskAttachmentService;
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
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskAttachmentAdapter taskAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<TaskAttachmentResponse> uploadFiles(Long taskId, List<MultipartFile> files) {
        List<TaskAttachmentResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                TaskAttachmentResponse response = uploadFile(taskId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for task {}: {}", file.getOriginalFilename(), taskId, e.getMessage());
                // Continue with other files even if one fails
            }
        }
        
        return responses;
    }

    @Override
    public TaskAttachmentResponse uploadFile(Long taskId, MultipartFile file) {
        // Validar se a tarefa existe
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + taskId));

        // Validar arquivo
        validateFile(file);

        try {
            // Gerar nome único para o arquivo
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = buildFilePath(taskId, fileName);

            // Fazer upload do arquivo
            String uploadedFilePath = fileStorageStrategy.uploadFile(file, filePath);

            // Criar registro no banco
            TaskAttachment attachment = TaskAttachment.builder()
                    .task(task)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(uploadedFilePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);
            
            log.info("File uploaded successfully: {} for task {}", fileName, taskId);
            return taskAttachmentAdapter.toResponse(savedAttachment);

        } catch (IOException e) {
            log.error("Error uploading file for task {}: {}", taskId, e.getMessage());
            throw new RuntimeException("Falha ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachmentResponse> getTaskAttachments(Long taskId) {
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        return taskAttachmentAdapter.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAttachment> getTaskAttachmentsEntities(Long taskId) {
        return taskAttachmentRepository.findByTaskId(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAttachmentResponse getAttachmentById(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
        
        return taskAttachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));

        try {
            return new InputStreamResource(fileStorageStrategy.getFileStream(attachment.getFilePath()));
        } catch (IOException e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            throw new RuntimeException("Falha ao baixar anexo: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));

        // DELETE físico no banco
        taskAttachmentRepository.delete(attachment);
        log.info("Attachment deleted from database: {}", attachment.getOriginalFileName());

        // Deletar arquivo do storage
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
                // Continue with other attachments
            }
        }
    }

    @Override
    public void deleteAllTaskAttachments(Long taskId) {
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        
        // DELETE físico de todos os anexos
        for (TaskAttachment attachment : attachments) {
            taskAttachmentRepository.delete(attachment);
            
            // Tentar deletar arquivo do storage
            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
                log.info("File deleted from storage: {}", attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
        }
        
        log.info("Deleted {} attachments from database for task {}", attachments.size(), taskId);
    }

    @Override
    public void deleteAllTaskAttachmentsAndFolder(Long taskId) {
        log.info("Physically deleting all attachments and folder for task ID: {}", taskId);
        
        // Buscar todos os anexos da tarefa
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        
        // Fazer DELETE físico de todos os anexos no banco
        if (!attachments.isEmpty()) {
            taskAttachmentRepository.deleteAll(attachments);
            log.info("Physically deleted {} attachments from database for task {}", attachments.size(), taskId);
        }
        
        // Excluir toda a pasta do storage S3
        String folderPath = "tasks/" + taskId + "/";
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
            throw new RuntimeException("Arquivo não pode estar vazio");
        }

        // Validar tamanho máximo (10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo: 10MB");
        }

        // Validar tipos permitidos
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // Se o tipo MIME não for reconhecido, validar por extensão
        if (contentType == null || contentType.isEmpty() || contentType.equals("application/octet-stream") || !isAllowedContentType(contentType)) {
            if (originalFilename != null && isAllowedByExtension(originalFilename)) {
                // Arquivo permitido pela extensão
                return;
            }
            throw new RuntimeException("Tipo de arquivo não permitido: " + contentType + " (arquivo: " + originalFilename + ")");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        List<String> allowedTypes = List.of(
            // Documentos
            "application/pdf",
            "application/msword",  // Word .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // Word .docx
            "application/vnd.ms-excel",  // Excel .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // Excel .xlsx
            "application/vnd.ms-powerpoint",  // PowerPoint .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // PowerPoint .pptx
            "text/plain",
            "text/csv",
            "application/json",  // JSON
            // Imagens
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            // Vídeos
            "video/mp4",
            "video/avi",
            "video/quicktime",
            "video/x-msvideo",
            // Arquivos compactados
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
        return UUID.randomUUID().toString() + extension;
    }

    private String buildFilePath(Long taskId, String fileName) {
        return String.format("tasks/%d/attachments/%s", taskId, fileName);
    }
}