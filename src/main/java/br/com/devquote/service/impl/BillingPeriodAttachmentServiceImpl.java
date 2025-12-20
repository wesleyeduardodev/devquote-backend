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

            }
        }

        return responses;
    }

    @Override
    public BillingPeriodAttachmentResponse uploadFile(Long billingPeriodId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        BillingPeriod billingPeriod = billingPeriodRepository.findById(billingPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("BillingPeriod not found with id: " + billingPeriodId));

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID() + extension;

            String storagePath = "billing-periods/" + billingPeriodId + "/attachments/" + fileName;

            String filePath = fileStorageStrategy.uploadFile(file, storagePath);

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

            fileStorageStrategy.deleteFile(attachment.getFilePath());

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

            }
        }
    }

    @Override
    public void deleteAllBillingPeriodAttachments(Long billingPeriodId) {
        List<BillingPeriodAttachment> attachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);

        for (BillingPeriodAttachment attachment : attachments) {

            attachment.setExcluded(true);
            billingPeriodAttachmentRepository.save(attachment);
        }

        log.info("All attachments soft deleted for billing period: {}", billingPeriodId);
    }

    @Override
    public void deleteAllBillingPeriodAttachmentsAndFolder(Long billingPeriodId) {
        try {
            log.debug("Starting deletion of all attachments for billing period: {}", billingPeriodId);

            List<BillingPeriodAttachment> attachments = billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
            log.debug("Found {} attachments to delete for billing period: {}", attachments.size(), billingPeriodId);

            if (!attachments.isEmpty()) {

                String folderPath = "billing-periods/" + billingPeriodId + "/attachments/";
                log.debug("Deleting folder from S3: {}", folderPath);
                fileStorageStrategy.deleteFolder(folderPath);
                log.debug("S3 folder deleted successfully");

                log.debug("Deleting {} attachment records from database", attachments.size());
                billingPeriodAttachmentRepository.deleteAll(attachments);
                log.debug("Database records deleted successfully");

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
    public List<BillingPeriodAttachment> getBillingPeriodAttachmentsEntities(Long billingPeriodId) {
        return billingPeriodAttachmentRepository.findByBillingPeriodId(billingPeriodId);
    }
}
