package br.com.devquote.service.impl;

import br.com.devquote.adapter.DeliveryOperationalAttachmentAdapter;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.entity.DeliveryOperationalAttachment;
import br.com.devquote.entity.DeliveryOperationalItem;
import br.com.devquote.repository.DeliveryOperationalAttachmentRepository;
import br.com.devquote.repository.DeliveryOperationalItemRepository;
import br.com.devquote.service.DeliveryOperationalAttachmentService;
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
public class DeliveryOperationalAttachmentServiceImpl implements DeliveryOperationalAttachmentService {

    private final DeliveryOperationalAttachmentRepository attachmentRepository;
    private final DeliveryOperationalItemRepository operationalItemRepository;
    private final DeliveryOperationalAttachmentAdapter attachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<DeliveryOperationalAttachmentResponse> uploadFiles(Long operationalItemId, List<MultipartFile> files) throws IOException {
        List<DeliveryOperationalAttachmentResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                DeliveryOperationalAttachmentResponse response = uploadFile(operationalItemId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for operational item {}: {}", file.getOriginalFilename(), operationalItemId, e.getMessage());
            }
        }

        return responses;
    }

    private DeliveryOperationalAttachmentResponse uploadFile(Long operationalItemId, MultipartFile file) throws IOException {
        DeliveryOperationalItem operationalItem = operationalItemRepository.findById(operationalItemId)
                .orElseThrow(() -> new RuntimeException("Item operacional não encontrado com ID: " + operationalItemId));

        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = buildFilePath(operationalItem.getDelivery().getId(), operationalItemId, fileName);

        String uploadedFilePath = fileStorageStrategy.uploadFile(file, filePath);

        DeliveryOperationalAttachment attachment = DeliveryOperationalAttachment.builder()
                .deliveryOperationalItem(operationalItem)
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(uploadedFilePath)
                .uploadedAt(LocalDateTime.now())
                .build();

        DeliveryOperationalAttachment saved = attachmentRepository.save(attachment);

        log.info("File uploaded successfully: {} for operational item {}", fileName, operationalItemId);
        return attachmentAdapter.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOperationalAttachmentResponse findById(Long id) {
        DeliveryOperationalAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + id));
        return attachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOperationalAttachmentResponse> findByOperationalItemId(Long operationalItemId) {
        List<DeliveryOperationalAttachment> attachments = attachmentRepository.findByDeliveryOperationalItemId(operationalItemId);
        return attachmentAdapter.toResponseList(attachments);
    }

    @Override
    public void delete(Long id) throws IOException {
        DeliveryOperationalAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + id));

        attachmentRepository.delete(attachment);
        log.info("Attachment deleted from database: {}", attachment.getOriginalName());

        try {
            fileStorageStrategy.deleteFile(attachment.getFilePath());
            log.info("File deleted from storage: {}", attachment.getFilePath());
        } catch (Exception e) {
            log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) throws IOException {
        DeliveryOperationalAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com ID: " + id));

        return new InputStreamResource(fileStorageStrategy.getFileStream(attachment.getFilePath()));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo não pode estar vazio");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo: 10MB");
        }

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || contentType.isEmpty() || contentType.equals("application/octet-stream") || !isAllowedContentType(contentType)) {
            if (originalFilename != null && isAllowedByExtension(originalFilename)) {
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
        return UUID.randomUUID().toString() + extension;
    }

    private String buildFilePath(Long deliveryId, Long operationalItemId, String fileName) {
        return String.format("deliveries/%d/operational-items/%d/attachments/%s", deliveryId, operationalItemId, fileName);
    }

    @Override
    public void deleteAllOperationalAttachmentsByDeliveryId(Long deliveryId) throws IOException {
        // Buscar todos os itens operacionais da delivery
        List<DeliveryOperationalItem> operationalItems = operationalItemRepository.findByDeliveryId(deliveryId);

        for (DeliveryOperationalItem item : operationalItems) {
            // Buscar todos os anexos deste item operacional
            List<DeliveryOperationalAttachment> attachments = attachmentRepository.findByDeliveryOperationalItemId(item.getId());

            for (DeliveryOperationalAttachment attachment : attachments) {
                try {
                    // Deletar arquivo do storage
                    fileStorageStrategy.deleteFile(attachment.getFilePath());
                    log.info("File deleted from storage: {}", attachment.getFilePath());
                } catch (Exception e) {
                    log.warn("Could not delete file from storage: {} - {}", attachment.getFilePath(), e.getMessage());
                }

                // Deletar do banco de dados
                attachmentRepository.delete(attachment);
                log.info("Attachment deleted from database: {}", attachment.getOriginalName());
            }
        }

        log.info("All operational attachments deleted for delivery ID: {}", deliveryId);
    }
}
