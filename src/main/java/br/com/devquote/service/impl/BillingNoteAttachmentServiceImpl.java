package br.com.devquote.service.impl;
import br.com.devquote.adapter.BillingNoteAttachmentAdapter;
import br.com.devquote.dto.response.BillingNoteAttachmentResponse;
import br.com.devquote.entity.BillingNote;
import br.com.devquote.entity.BillingNoteAttachment;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.BillingNoteAttachmentRepository;
import br.com.devquote.repository.BillingNoteRepository;
import br.com.devquote.service.BillingNoteAttachmentService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingNoteAttachmentServiceImpl implements BillingNoteAttachmentService {

    private final BillingNoteAttachmentRepository billingNoteAttachmentRepository;
    private final BillingNoteRepository billingNoteRepository;
    private final BillingNoteAttachmentAdapter billingNoteAttachmentAdapter;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<BillingNoteAttachmentResponse> uploadFiles(Long billingNoteId, List<MultipartFile> files) {
        List<BillingNoteAttachmentResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                responses.add(uploadFile(billingNoteId, file));
            } catch (Exception e) {
                log.error("Error uploading file {} for billing note {}: {}", file.getOriginalFilename(), billingNoteId, e.getMessage());
            }
        }

        return responses;
    }

    @Override
    public BillingNoteAttachmentResponse uploadFile(Long billingNoteId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        BillingNote billingNote = billingNoteRepository.findById(billingNoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação de faturamento", billingNoteId));

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID() + extension;

            String storagePath = buildFolderPath(billingNoteId) + fileName;
            String filePath = fileStorageStrategy.uploadFile(file, storagePath);

            BillingNoteAttachment attachment = BillingNoteAttachment.builder()
                    .billingNote(billingNote)
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(filePath)
                    .excluded(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            BillingNoteAttachment saved = billingNoteAttachmentRepository.save(attachment);
            log.info("File uploaded successfully: {} for billing note: {}", originalFileName, billingNoteId);

            return billingNoteAttachmentAdapter.toResponse(saved);

        } catch (IOException e) {
            log.error("Error storing file: {}", e.getMessage());
            throw new RuntimeException("Error storing file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingNoteAttachmentResponse> getBillingNoteAttachments(Long billingNoteId) {
        return billingNoteAttachmentAdapter.toResponseList(billingNoteAttachmentRepository.findByBillingNoteId(billingNoteId));
    }

    @Override
    @Transactional(readOnly = true)
    public BillingNoteAttachmentResponse getAttachmentById(Long attachmentId) {
        return billingNoteAttachmentAdapter.toResponse(findAttachmentOrThrow(attachmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        BillingNoteAttachment attachment = findAttachmentOrThrow(attachmentId);

        try {
            return new InputStreamResource(fileStorageStrategy.getFileStream(attachment.getFilePath()));
        } catch (IOException e) {
            log.error("Error loading file: {}", e.getMessage());
            throw new RuntimeException("Error loading file: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        BillingNoteAttachment attachment = findAttachmentOrThrow(attachmentId);

        try {
            fileStorageStrategy.deleteFile(attachment.getFilePath());
            billingNoteAttachmentRepository.delete(attachment);
            log.info("Attachment deleted successfully: {}", attachment.getOriginalFileName());
        } catch (Exception e) {
            log.error("Error deleting attachment: {}", e.getMessage());
            throw new RuntimeException("Error deleting attachment: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllByBillingNote(Long billingNoteId) {
        List<BillingNoteAttachment> attachments = billingNoteAttachmentRepository.findByBillingNoteId(billingNoteId);
        if (attachments.isEmpty()) {
            return;
        }

        try {
            fileStorageStrategy.deleteFolder(buildFolderPath(billingNoteId));
        } catch (Exception e) {
            log.error("Error deleting storage folder of billing note {}: {}", billingNoteId, e.getMessage());
        }

        billingNoteAttachmentRepository.deleteAll(attachments);
        log.debug("BILLING_NOTE_ATTACHMENT DELETE_ALL billingNoteId={} removed={}", billingNoteId, attachments.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countByBillingNoteIds(List<Long> billingNoteIds) {
        Map<Long, Integer> counts = new HashMap<>();
        if (billingNoteIds == null || billingNoteIds.isEmpty()) {
            return counts;
        }

        for (Object[] row : billingNoteAttachmentRepository.countGroupedByBillingNoteIds(billingNoteIds)) {
            counts.put((Long) row[0], ((Long) row[1]).intValue());
        }

        return counts;
    }

    private BillingNoteAttachment findAttachmentOrThrow(Long attachmentId) {
        return billingNoteAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo de anotação", attachmentId));
    }

    private String buildFolderPath(Long billingNoteId) {
        return "billing-notes/" + billingNoteId + "/attachments/";
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
}
