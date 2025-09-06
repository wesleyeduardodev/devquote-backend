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
                    .excluded(false)
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
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskIdAndNotExcluded(taskId);
        return taskAttachmentAdapter.toResponseList(attachments);
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

        if (attachment.getExcluded()) {
            throw new RuntimeException("Anexo foi excluído e não pode ser baixado");
        }

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

        // Soft delete - marca como excluído
        attachment.setExcluded(true);
        taskAttachmentRepository.save(attachment);

        // Tentar deletar arquivo do storage (não bloqueia se falhar)
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
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskIdAndNotExcluded(taskId);
        
        for (TaskAttachment attachment : attachments) {
            attachment.setExcluded(true);
            taskAttachmentRepository.save(attachment);
            
            // Tentar deletar arquivo do storage
            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
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
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new RuntimeException("Tipo de arquivo não permitido: " + contentType);
        }
    }

    private boolean isAllowedContentType(String contentType) {
        List<String> allowedTypes = List.of(
            // Documentos
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv",
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