package br.com.devquote.controller;

import br.com.devquote.controller.doc.BillingNoteAttachmentControllerDoc;
import br.com.devquote.dto.response.BillingNoteAttachmentResponse;
import br.com.devquote.service.BillingNoteAttachmentService;
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
@RequestMapping("/api/billing-note-attachments")
@RequiredArgsConstructor
@Slf4j
public class BillingNoteAttachmentController implements BillingNoteAttachmentControllerDoc {

    private final BillingNoteAttachmentService billingNoteAttachmentService;

    @PostMapping(value = "/upload/{billingNoteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillingNoteAttachmentResponse>> uploadFiles(
            @PathVariable Long billingNoteId,
            @RequestParam("files") List<MultipartFile> files) {

        try {
            return ResponseEntity.ok(billingNoteAttachmentService.uploadFiles(billingNoteId, files));
        } catch (Exception e) {
            log.error("Error uploading files for billing note {}: {}", billingNoteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/billing-note/{billingNoteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingNoteAttachmentResponse>> getBillingNoteAttachments(@PathVariable Long billingNoteId) {
        return ResponseEntity.ok(billingNoteAttachmentService.getBillingNoteAttachments(billingNoteId));
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        BillingNoteAttachmentResponse attachment = billingNoteAttachmentService.getAttachmentById(attachmentId);
        Resource resource = billingNoteAttachmentService.downloadAttachment(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        billingNoteAttachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
