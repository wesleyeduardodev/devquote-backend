package br.com.devquote.controller;

import br.com.devquote.dto.response.SubTaskAttachmentResponse;
import br.com.devquote.service.SubTaskAttachmentService;
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
@RequestMapping("/api/subtask-attachments")
@RequiredArgsConstructor
@Slf4j
public class SubTaskAttachmentController {

    private final SubTaskAttachmentService subTaskAttachmentService;

    @PostMapping(value = "/upload/{subTaskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<SubTaskAttachmentResponse>> uploadFiles(
            @PathVariable Long subTaskId,
            @RequestParam("files") List<MultipartFile> files) {

        try {
            List<SubTaskAttachmentResponse> responses = subTaskAttachmentService.uploadFiles(subTaskId, files);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error uploading files for subtask {}: {}", subTaskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload-single/{subTaskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SubTaskAttachmentResponse> uploadFile(
            @PathVariable Long subTaskId,
            @RequestParam("file") MultipartFile file) {

        try {
            SubTaskAttachmentResponse response = subTaskAttachmentService.uploadFile(subTaskId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file for subtask {}: {}", subTaskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/subtask/{subTaskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<SubTaskAttachmentResponse>> getSubTaskAttachments(@PathVariable Long subTaskId) {
        try {
            List<SubTaskAttachmentResponse> attachments = subTaskAttachmentService.getSubTaskAttachments(subTaskId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error getting attachments for subtask {}: {}", subTaskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SubTaskAttachmentResponse> getAttachment(@PathVariable Long attachmentId) {
        try {
            SubTaskAttachmentResponse attachment = subTaskAttachmentService.getAttachmentById(attachmentId);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            log.error("Error getting attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            SubTaskAttachmentResponse attachment = subTaskAttachmentService.getAttachmentById(attachmentId);
            Resource resource = subTaskAttachmentService.downloadAttachment(attachmentId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, attachment.getContentType())
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            subTaskAttachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.ok("Anexo excluido com sucesso");
        } catch (Exception e) {
            log.error("Error deleting attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir anexo: " + e.getMessage());
        }
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteAttachments(@RequestBody List<Long> attachmentIds) {
        try {
            subTaskAttachmentService.deleteAttachments(attachmentIds);
            return ResponseEntity.ok("Anexos excluidos com sucesso");
        } catch (Exception e) {
            log.error("Error deleting attachments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir anexos: " + e.getMessage());
        }
    }

    @DeleteMapping("/subtask/{subTaskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteAllSubTaskAttachments(@PathVariable Long subTaskId) {
        try {
            subTaskAttachmentService.deleteAllSubTaskAttachments(subTaskId);
            return ResponseEntity.ok("Todos os anexos da subtarefa foram excluidos");
        } catch (Exception e) {
            log.error("Error deleting all attachments for subtask {}: {}", subTaskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir anexos da subtarefa: " + e.getMessage());
        }
    }
}
