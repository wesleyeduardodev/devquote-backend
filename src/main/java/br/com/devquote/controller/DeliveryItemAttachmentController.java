package br.com.devquote.controller;

import br.com.devquote.dto.response.DeliveryItemAttachmentResponse;
import br.com.devquote.service.DeliveryItemAttachmentService;
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
@RequestMapping("/api/delivery-item-attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Item Attachments", description = "API para gestão de anexos de itens de entregas")
public class DeliveryItemAttachmentController {

    private final DeliveryItemAttachmentService deliveryItemAttachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de arquivos para um item de entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryItemAttachmentResponse>> uploadFiles(
            @Parameter(description = "ID do item de entrega") @RequestParam("deliveryItemId") Long deliveryItemId,
            @Parameter(description = "Arquivos para upload") @RequestParam("files") List<MultipartFile> files) {
        
        log.info("Upload request for delivery item {} with {} files", deliveryItemId, files.size());
        List<DeliveryItemAttachmentResponse> responses = deliveryItemAttachmentService.uploadFiles(deliveryItemId, files);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/delivery-item/{deliveryItemId}")
    @Operation(summary = "Listar anexos de um item de entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryItemAttachmentResponse>> getDeliveryItemAttachments(
            @Parameter(description = "ID do item de entrega") @PathVariable Long deliveryItemId) {
        
        List<DeliveryItemAttachmentResponse> attachments = deliveryItemAttachmentService.getDeliveryItemAttachments(deliveryItemId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Buscar anexo por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryItemAttachmentResponse> getAttachmentById(
            @Parameter(description = "ID do anexo") @PathVariable Long attachmentId) {
        
        DeliveryItemAttachmentResponse attachment = deliveryItemAttachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Fazer download de um anexo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "ID do anexo") @PathVariable Long attachmentId) {
        
        try {
            DeliveryItemAttachmentResponse attachmentInfo = deliveryItemAttachmentService.getAttachmentById(attachmentId);
            Resource resource = deliveryItemAttachmentService.downloadAttachment(attachmentId);

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
        
        deliveryItemAttachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Excluir múltiplos anexos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAttachments(
            @Parameter(description = "Lista de IDs dos anexos") @RequestBody List<Long> attachmentIds) {
        
        deliveryItemAttachmentService.deleteAttachments(attachmentIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delivery-item/{deliveryItemId}")
    @Operation(summary = "Excluir todos os anexos de um item de entrega")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAllDeliveryItemAttachments(
            @Parameter(description = "ID do item de entrega") @PathVariable Long deliveryItemId) {
        
        deliveryItemAttachmentService.deleteAllDeliveryItemAttachments(deliveryItemId);
        return ResponseEntity.noContent().build();
    }
}