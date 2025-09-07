package br.com.devquote.service.impl;

import br.com.devquote.adapter.BillingPeriodAttachmentAdapter;
import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.BillingPeriodAttachment;
import br.com.devquote.repository.BillingPeriodAttachmentRepository;
import br.com.devquote.repository.BillingPeriodRepository;
import br.com.devquote.service.BillingPeriodAttachmentService;
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
public class BillingPeriodAttachmentServiceImpl implements BillingPeriodAttachmentService {

    private final BillingPeriodAttachmentRepository billingPeriodAttachmentRepository;
    private final BillingPeriodRepository billingPeriodRepository;
    private final BillingPeriodAttachmentAdapter billingPeriodAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<BillingPeriodAttachmentResponse> uploadFiles(Long billingPeriodId, List<MultipartFile> files) {
        List<BillingPeriodAttachmentResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                BillingPeriodAttachmentResponse response = uploadFile(billingPeriodId, file);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {} for billing period {}: {}", file.getOriginalFilename(), billingPeriodId, e.getMessage());
                // Continue with other files even if one fails
            }
        }
        
        return responses;
    }

    @Override
    public BillingPeriodAttachmentResponse uploadFile(Long billingPeriodId, MultipartFile file) {
        // Validation
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Verify billing period exists
        BillingPeriod billingPeriod = billingPeriodRepository.findById(billingPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("BillingPeriod not found with id: " + billingPeriodId));

        try {
            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + extension;
            
            // Define storage path: billing-periods/{billingPeriodId}/{fileName}
            String storagePath = "billing-periods/" + billingPeriodId + "/" + fileName;
            
            // Store file
            String filePath = fileStorageStrategy.uploadFile(file, storagePath);
            
            // Save attachment info in database
            BillingPeriodAttachment attachment = BillingPeriodAttachment.builder()
                    .billingPeriod(billingPeriod)
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(filePath)
                    .excluded(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            BillingPeriodAttachment savedAttachment = billingPeriodAttachmentRepository.save(attachment);
            
            log.info("File uploaded successfully: {} for billing period: {}", originalFileName, billingPeriodId);
            
            return billingPeriodAttachmentAdapter.toResponse(savedAttachment);
            
        } catch (IOException e) {
            log.error("Error storing file: {}", e.getMessage());
            throw new RuntimeException("Error storing file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingPeriodAttachmentResponse> getBillingPeriodAttachments(Long billingPeriodId) {
        List<BillingPeriodAttachment> attachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
        return billingPeriodAttachmentAdapter.toResponseList(attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingPeriodAttachmentResponse getAttachmentById(Long attachmentId) {
        BillingPeriodAttachment attachment = billingPeriodAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));
        
        return billingPeriodAttachmentAdapter.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        BillingPeriodAttachment attachment = billingPeriodAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

        try {
            return new InputStreamResource(fileStorageStrategy.getFileStream(attachment.getFilePath()));
        } catch (IOException e) {
            log.error("Error loading file: {}", e.getMessage());
            throw new RuntimeException("Error loading file: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        BillingPeriodAttachment attachment = billingPeriodAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));

        try {
            // Delete from storage
            fileStorageStrategy.deleteFile(attachment.getFilePath());
            
            // Delete from database
            billingPeriodAttachmentRepository.delete(attachment);
            
            log.info("Attachment deleted successfully: {}", attachment.getOriginalFileName());
            
        } catch (Exception e) {
            log.error("Error deleting attachment: {}", e.getMessage());
            throw new RuntimeException("Error deleting attachment: " + e.getMessage());
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
    public void deleteAllBillingPeriodAttachments(Long billingPeriodId) {
        List<BillingPeriodAttachment> attachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
        
        for (BillingPeriodAttachment attachment : attachments) {
            // Soft delete - mark as excluded
            attachment.setExcluded(true);
            billingPeriodAttachmentRepository.save(attachment);
        }
        
        log.info("All attachments soft deleted for billing period: {}", billingPeriodId);
    }

    @Override
    public void deleteAllBillingPeriodAttachmentsAndFolder(Long billingPeriodId) {
        try {
            log.debug("Starting deletion of all attachments for billing period: {}", billingPeriodId);
            
            // 1. Find all attachments first
            List<BillingPeriodAttachment> attachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
            log.debug("Found {} attachments to delete for billing period: {}", attachments.size(), billingPeriodId);
            
            if (!attachments.isEmpty()) {
                // 2. Delete files from S3 storage
                String folderPath = "billing-periods/" + billingPeriodId + "/";
                log.debug("Deleting folder from S3: {}", folderPath);
                fileStorageStrategy.deleteFolder(folderPath);
                log.debug("S3 folder deleted successfully");
                
                // 3. Delete records from database
                log.debug("Deleting {} attachment records from database", attachments.size());
                billingPeriodAttachmentRepository.deleteAll(attachments);
                log.debug("Database records deleted successfully");
                
                // 4. Verify deletion
                List<BillingPeriodAttachment> remainingAttachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
                if (!remainingAttachments.isEmpty()) {
                    throw new RuntimeException("Failed to delete all attachments. " + remainingAttachments.size() + " records still remain");
                }
                
                log.info("Successfully deleted {} attachments and folder for billing period: {}", attachments.size(), billingPeriodId);
            } else {
                log.debug("No attachments found for billing period: {}", billingPeriodId);
            }
            
        } catch (Exception e) {
            log.error("CRITICAL ERROR deleting attachments for billing period {}: {}", billingPeriodId, e.getMessage(), e);
            throw new RuntimeException("Error deleting all attachments and folder: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<br.com.devquote.entity.BillingPeriodAttachment> getBillingPeriodAttachmentsEntities(Long billingPeriodId) {
        return billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
    }
}