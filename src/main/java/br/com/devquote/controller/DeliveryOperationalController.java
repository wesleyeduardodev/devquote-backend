package br.com.devquote.controller;

import br.com.devquote.dto.request.DeliveryOperationalItemRequest;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.dto.response.DeliveryOperationalItemResponse;
import br.com.devquote.service.DeliveryOperationalAttachmentService;
import br.com.devquote.service.DeliveryOperationalItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/delivery-operational")
@RequiredArgsConstructor
public class DeliveryOperationalController {

    private final DeliveryOperationalItemService operationalItemService;
    private final DeliveryOperationalAttachmentService attachmentService;

    // ========== CRUD de Itens Operacionais ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryOperationalItemResponse> createItem(@RequestBody @Valid DeliveryOperationalItemRequest request) {
        return new ResponseEntity<>(operationalItemService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryOperationalItemResponse> updateItem(
            @PathVariable Long id,
            @RequestBody @Valid DeliveryOperationalItemRequest request) {
        return ResponseEntity.ok(operationalItemService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryOperationalItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(operationalItemService.findById(id));
    }

    @GetMapping("/delivery/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryOperationalItemResponse>> getItemsByDelivery(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(operationalItemService.findByDeliveryId(deliveryId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        operationalItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Anexos ==========

    @PostMapping("/{itemId}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryOperationalAttachmentResponse>> uploadAttachments(
            @PathVariable Long itemId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        return new ResponseEntity<>(attachmentService.uploadFiles(itemId, files), HttpStatus.CREATED);
    }

    @GetMapping("/{itemId}/attachments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<DeliveryOperationalAttachmentResponse>> getAttachments(@PathVariable Long itemId) {
        return ResponseEntity.ok(attachmentService.findByOperationalItemId(itemId));
    }

    @GetMapping("/attachments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) throws IOException {
        DeliveryOperationalAttachmentResponse attachment = attachmentService.findById(id);
        Resource resource = attachmentService.downloadFile(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(attachment.getContentType()));
        headers.setContentDispositionFormData("attachment", attachment.getOriginalName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) throws IOException {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
