package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryItemAttachmentAdapter;
import br.com.devquote.dto.response.DeliveryItemAttachmentResponse;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.entity.DeliveryItemAttachment;
import br.com.devquote.repository.DeliveryItemAttachmentRepository;
import br.com.devquote.repository.DeliveryItemRepository;
import br.com.devquote.service.DeliveryItemAttachmentService;
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
public class DeliveryItemAttachmentServiceImpl implements DeliveryItemAttachmentService {

    private final DeliveryItemAttachmentRepository deliveryItemAttachmentRepository;
    private final DeliveryItemRepository deliveryItemRepository;
    private final DeliveryItemAttachmentAdapter deliveryItemAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<DeliveryItemAttachmentResponse> uploadFiles(Long deliveryItemId, List<MultipartFile> files) {
        List<DeliveryItemAttachmentResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                DeliveryItemAttachmentResponse response = uploadFile(deliveryItemId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for delivery item {}: {}", file.getOriginalFilename(), deliveryItemId, e.getMessage());
            }
        }
        
        return responses;
    }

    @Override
    public DeliveryItemAttachmentResponse uploadFile(Long deliveryItemId, MultipartFile file) {
        DeliveryItem deliveryItem = deliveryItemRepository.findById(deliveryItemId)
                .orElseThrow(() -> new RuntimeException("Item de entrega não encontrado com ID: " + deliveryItemId));

        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = buildFilePath(deliveryItem.getDelivery().getId(), deliveryItemId, fileName);

            String uploadedFilePath = fileStorageStrategy.uploadFile(file, filePath);

            DeliveryItemAttachment attachment = DeliveryItemAttachment.builder()
                    .deliveryItem(deliveryItem)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(uploadedFilePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            DeliveryItemAttachment savedAttachment = deliveryItemAttachmentRepository.save(attachment);
            
            log.info("File uploaded successfully: {} for delivery item {}", fileName, deliveryItemId);
            return deliveryItemAttachmentAdapter.toResponse(savedAttachment);

        } catch (IOException e) {
            log.error("Error uploading file for delivery item {}: {}", deliveryItemId, e.getMessage());
            throw new RuntimeException("Falha ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryItemAttachmentResponse> getDeliveryItemAttachments(Long deliveryItemId) {
        List<DeliveryItemAttachment> attachments = deliveryItemAttachmentRepository.findByDeliveryItemId(deliveryItemId);
        return deliveryItemAttachmentAdapter.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryItemAttachmentResponse getAttachmentById(Long attachmentId) {
        DeliveryItemAttachment attachment = deliveryItemAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
        
        return deliveryItemAttachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        DeliveryItemAttachment attachment = deliveryItemAttachmentRepository.findById(attachmentId)
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
        DeliveryItemAttachment attachment = deliveryItemAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));

        deliveryItemAttachmentRepository.delete(attachment);
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
    public void deleteAllDeliveryItemAttachments(Long deliveryItemId) {
        List<DeliveryItemAttachment> attachments = deliveryItemAttachmentRepository.findByDeliveryItemId(deliveryItemId);
        
        for (DeliveryItemAttachment attachment : attachments) {
            deliveryItemAttachmentRepository.delete(attachment);
            
            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
                log.info("File deleted from storage: {}", attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
        }
        
        log.info("Deleted {} attachments from database for delivery item {}", attachments.size(), deliveryItemId);
    }

    @Override
    public void deleteAllDeliveryItemAttachmentsByDeliveryId(Long deliveryId) {
        List<DeliveryItemAttachment> attachments = deliveryItemAttachmentRepository.findByDeliveryId(deliveryId);
        
        for (DeliveryItemAttachment attachment : attachments) {
            deliveryItemAttachmentRepository.delete(attachment);
            
            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
                log.info("File deleted from storage: {}", attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
        }
        
        log.info("Deleted {} item attachments from database for delivery {}", attachments.size(), deliveryId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo não pode estar vazio");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo: 10MB");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || contentType.isEmpty() || contentType.equals("application/octet-stream") || !isAllowedContentType(contentType)) {
            if (isAllowedByExtension(originalFilename)) {
                return;
            }
            throw new RuntimeException("Tipo de arquivo não permitido: " + contentType + " (arquivo: " + originalFilename + ")");
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

    private String buildFilePath(Long deliveryId, Long deliveryItemId, String fileName) {
        return String.format("deliveries/%d/development-items/%d/attachments/%s", deliveryId, deliveryItemId, fileName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<br.com.devquote.entity.DeliveryItemAttachment> getDeliveryItemAttachmentsEntitiesByDeliveryId(Long deliveryId) {
        return deliveryItemAttachmentRepository.findByDeliveryId(deliveryId);
    }
}