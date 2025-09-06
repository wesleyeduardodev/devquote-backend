package br.com.devquote.controller;

import br.com.devquote.controller.doc.TaskAttachmentControllerDoc;
import br.com.devquote.dto.response.TaskAttachmentResponse;
import br.com.devquote.service.TaskAttachmentService;
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
@RequestMapping("/task-attachments")
@RequiredArgsConstructor
@Slf4j
public class TaskAttachmentController implements TaskAttachmentControllerDoc {

    private final TaskAttachmentService taskAttachmentService;

    @PostMapping(value = "/upload/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<TaskAttachmentResponse>> uploadFiles(
            @PathVariable Long taskId,
            @RequestParam("files") List<MultipartFile> files) {
        
        try {
            List<TaskAttachmentResponse> responses = taskAttachmentService.uploadFiles(taskId, files);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error uploading files for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/upload-single/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskAttachmentResponse> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            TaskAttachmentResponse response = taskAttachmentService.uploadFile(taskId, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<TaskAttachmentResponse>> getTaskAttachments(@PathVariable Long taskId) {
        try {
            List<TaskAttachmentResponse> attachments = taskAttachmentService.getTaskAttachments(taskId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error getting attachments for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskAttachmentResponse> getAttachment(@PathVariable Long attachmentId) {
        try {
            TaskAttachmentResponse attachment = taskAttachmentService.getAttachmentById(attachmentId);
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
            TaskAttachmentResponse attachment = taskAttachmentService.getAttachmentById(attachmentId);
            Resource resource = taskAttachmentService.downloadAttachment(attachmentId);

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
            taskAttachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.ok("Anexo excluído com sucesso");
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
            taskAttachmentService.deleteAttachments(attachmentIds);
            return ResponseEntity.ok("Anexos excluídos com sucesso");
        } catch (Exception e) {
            log.error("Error deleting attachments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir anexos: " + e.getMessage());
        }
    }

    @DeleteMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> deleteAllTaskAttachments(@PathVariable Long taskId) {
        try {
            taskAttachmentService.deleteAllTaskAttachments(taskId);
            return ResponseEntity.ok("Todos os anexos da tarefa foram excluídos");
        } catch (Exception e) {
            log.error("Error deleting all attachments for task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir anexos da tarefa: " + e.getMessage());
        }
    }
}