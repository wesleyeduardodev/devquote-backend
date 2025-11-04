package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAttachmentAdapter;
import br.com.devquote.dto.response.DeliveryAttachmentResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryAttachment;
import br.com.devquote.repository.DeliveryAttachmentRepository;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.DeliveryAttachmentService;
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
public class DeliveryAttachmentServiceImpl implements DeliveryAttachmentService {

    private final DeliveryAttachmentRepository deliveryAttachmentRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryAttachmentAdapter deliveryAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<DeliveryAttachmentResponse> uploadFiles(Long deliveryId, List<MultipartFile> files) {
        List<DeliveryAttachmentResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                DeliveryAttachmentResponse response = uploadFile(deliveryId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for delivery {}: {}", file.getOriginalFilename(), deliveryId, e.getMessage());
            }
        }
        
        return responses;
    }

    @Override
    public DeliveryAttachmentResponse uploadFile(Long deliveryId, MultipartFile file) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada com ID: " + deliveryId));

        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = buildFilePath(deliveryId, fileName);

            String uploadedFilePath = fileStorageStrategy.uploadFile(file, filePath);

            DeliveryAttachment attachment = DeliveryAttachment.builder()
                    .delivery(delivery)
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(uploadedFilePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            DeliveryAttachment savedAttachment = deliveryAttachmentRepository.save(attachment);
            
            log.info("File uploaded successfully: {} for delivery {}", fileName, deliveryId);
            return deliveryAttachmentAdapter.toResponse(savedAttachment);

        } catch (IOException e) {
            log.error("Error uploading file for delivery {}: {}", deliveryId, e.getMessage());
            throw new RuntimeException("Falha ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryAttachmentResponse> getDeliveryAttachments(Long deliveryId) {
        List<DeliveryAttachment> attachments = deliveryAttachmentRepository.findByDeliveryId(deliveryId);
        return deliveryAttachmentAdapter.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryAttachmentResponse getAttachmentById(Long attachmentId) {
        DeliveryAttachment attachment = deliveryAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));
        
        return deliveryAttachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        DeliveryAttachment attachment = deliveryAttachmentRepository.findById(attachmentId)
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
        DeliveryAttachment attachment = deliveryAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + attachmentId));

        deliveryAttachmentRepository.delete(attachment);
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
    public void deleteAllDeliveryAttachments(Long deliveryId) {
        List<DeliveryAttachment> attachments = deliveryAttachmentRepository.findByDeliveryId(deliveryId);
        
        for (DeliveryAttachment attachment : attachments) {
            deliveryAttachmentRepository.delete(attachment);
            
            try {
                fileStorageStrategy.deleteFile(attachment.getFilePath());
                log.info("File deleted from storage: {}", attachment.getFilePath());
            } catch (Exception e) {
                log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
            }
        }
        
        log.info("Deleted {} attachments from database for delivery {}", attachments.size(), deliveryId);
    }

    @Override
    public void deleteAllDeliveryAttachmentsAndFolder(Long deliveryId) {
        log.info("Physically deleting all attachments and folder for delivery ID: {}", deliveryId);
        
        List<DeliveryAttachment> attachments = deliveryAttachmentRepository.findByDeliveryId(deliveryId);
        
        if (!attachments.isEmpty()) {
            deliveryAttachmentRepository.deleteAll(attachments);
            log.info("Physically deleted {} attachments from database for delivery {}", attachments.size(), deliveryId);
        }
        
        String folderPath = "deliveries/" + deliveryId + "/attachments/";
        try {
            boolean deleted = fileStorageStrategy.deleteFolder(folderPath);
            if (deleted) {
                log.info("Successfully deleted delivery folder from storage: {}", folderPath);
            } else {
                log.warn("Failed to delete delivery folder from storage: {}", folderPath);
            }
        } catch (Exception e) {
            log.error("Error deleting delivery folder from storage: {} - {}", folderPath, e.getMessage());
        }
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

    private String buildFilePath(Long deliveryId, String fileName) {
        return String.format("deliveries/%d/attachments/%s", deliveryId, fileName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<br.com.devquote.entity.DeliveryAttachment> getDeliveryAttachmentsEntities(Long deliveryId) {
        return deliveryAttachmentRepository.findByDeliveryId(deliveryId);
    }
}