package br.com.devquote.controller;

import br.com.devquote.dto.response.DeliveryAttachmentResponse;
import br.com.devquote.service.DeliveryAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/delivery-attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Attachments", description = "API para gestão de anexos de entregas")
public class DeliveryAttachmentController {

    private final DeliveryAttachmentService deliveryAttachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de arquivos para uma entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryAttachmentResponse>> uploadFiles(
            @Parameter(description = "ID da entrega") @RequestParam("deliveryId") Long deliveryId,
            @Parameter(description = "Arquivos para upload") @RequestParam("files") List<MultipartFile> files) {
        
        log.info("Upload request for delivery {} with {} files", deliveryId, files.size());
        List<DeliveryAttachmentResponse> responses = deliveryAttachmentService.uploadFiles(deliveryId, files);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/delivery/{deliveryId}")
    @Operation(summary = "Listar anexos de uma entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryAttachmentResponse>> getDeliveryAttachments(
            @Parameter(description = "ID da entrega") @PathVariable Long deliveryId) {
        
        List<DeliveryAttachmentResponse> attachments = deliveryAttachmentService.getDeliveryAttachments(deliveryId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Buscar anexo por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryAttachmentResponse> getAttachmentById(
            @Parameter(description = "ID do anexo") @PathVariable Long attachmentId) {
        
        DeliveryAttachmentResponse attachment = deliveryAttachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Fazer download de um anexo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "ID do anexo") @PathVariable Long attachmentId) {
        
        try {
            DeliveryAttachmentResponse attachmentInfo = deliveryAttachmentService.getAttachmentById(attachmentId);
            Resource resource = deliveryAttachmentService.downloadAttachment(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachmentInfo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentInfo.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Excluir um anexo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "ID do anexo") @PathVariable Long attachmentId) {
        
        deliveryAttachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Excluir múltiplos anexos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAttachments(
            @Parameter(description = "Lista de IDs dos anexos") @RequestBody List<Long> attachmentIds) {
        
        deliveryAttachmentService.deleteAttachments(attachmentIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delivery/{deliveryId}")
    @Operation(summary = "Excluir todos os anexos de uma entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAllDeliveryAttachments(
            @Parameter(description = "ID da entrega") @PathVariable Long deliveryId) {
        
        deliveryAttachmentService.deleteAllDeliveryAttachments(deliveryId);
        return ResponseEntity.noContent().build();
    }
}