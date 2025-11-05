package br.com.devquote.controller;

import br.com.devquote.controller.doc.BillingPeriodAttachmentControllerDoc;
import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import br.com.devquote.service.BillingPeriodAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/billing-period-attachments")
@RequiredArgsConstructor
@Slf4j
public class BillingPeriodAttachmentController implements BillingPeriodAttachmentControllerDoc {

    private final BillingPeriodAttachmentService billingPeriodAttachmentService;

    @PostMapping(value = "/upload/{billingPeriodId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillingPeriodAttachmentResponse>> uploadFiles(
            @PathVariable Long billingPeriodId,
            @RequestParam("files") List<MultipartFile> files) {
        
        try {
            List<BillingPeriodAttachmentResponse> responses = billingPeriodAttachmentService.uploadFiles(billingPeriodId, files);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error uploading files for billing period {}: {}", billingPeriodId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload-single/{billingPeriodId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingPeriodAttachmentResponse> uploadFile(
            @PathVariable Long billingPeriodId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            BillingPeriodAttachmentResponse response = billingPeriodAttachmentService.uploadFile(billingPeriodId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file for billing period {}: {}", billingPeriodId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/billing-period/{billingPeriodId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodAttachmentResponse>> getBillingPeriodAttachments(@PathVariable Long billingPeriodId) {
        try {
            List<BillingPeriodAttachmentResponse> attachments = billingPeriodAttachmentService.getBillingPeriodAttachments(billingPeriodId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error getting attachments for billing period {}: {}", billingPeriodId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodAttachmentResponse> getAttachmentById(@PathVariable Long attachmentId) {
        try {
            BillingPeriodAttachmentResponse attachment = billingPeriodAttachmentService.getAttachmentById(attachmentId);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            log.error("Error getting attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            BillingPeriodAttachmentResponse attachment = billingPeriodAttachmentService.getAttachmentById(attachmentId);
            Resource resource = billingPeriodAttachmentService.downloadAttachment(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            billingPeriodAttachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAttachments(@RequestBody List<Long> attachmentIds) {
        try {
            billingPeriodAttachmentService.deleteAttachments(attachmentIds);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting attachments in bulk: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/billing-period/{billingPeriodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllBillingPeriodAttachments(@PathVariable Long billingPeriodId) {
        try {
            billingPeriodAttachmentService.deleteAllBillingPeriodAttachments(billingPeriodId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting all attachments for billing period {}: {}", billingPeriodId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}