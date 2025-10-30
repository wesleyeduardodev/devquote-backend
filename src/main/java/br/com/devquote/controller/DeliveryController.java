package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.DeliveryControllerDoc;
import br.com.devquote.dto.request.DeliveryNotesRequest;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.DeliveryStatusCount;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.service.DeliveryService;
import br.com.devquote.service.DeliveryAttachmentService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/deliveries")
@Validated
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDoc {

    private final DeliveryService deliveryService;
    private final DeliveryAttachmentService deliveryAttachmentService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "task.id", "task.title", "task.code", "status", "createdAt", "updatedAt"
    );

    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<DeliveryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {
        List<String> sortParams = params != null ? params.get("sort") : null;

        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );

        Page<DeliveryResponse> pageResult = deliveryService.findAllPaginated(
                id, taskName, taskCode, flowType, status, createdAt, updatedAt, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(pageResult));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> create(@RequestBody @Valid DeliveryRequest dto) {
        return new ResponseEntity<>(deliveryService.create(dto), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> createWithFiles(
            @RequestParam("dto") String dtoJson,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        try {
            // Deserializar o JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            DeliveryRequest deliveryRequest = objectMapper.readValue(dtoJson, DeliveryRequest.class);
            
            // Criar entrega
            DeliveryResponse delivery = deliveryService.create(deliveryRequest);
            
            // Fazer upload dos arquivos se fornecidos
            if (files != null && !files.isEmpty()) {
                deliveryAttachmentService.uploadFiles(delivery.getId(), files);
            }
            
            return new ResponseEntity<>(delivery, HttpStatus.CREATED);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar entrega com anexos: " + e.getMessage(), e);
        }
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> update(@PathVariable Long id, @RequestBody @Valid DeliveryRequest dto) {
        return ResponseEntity.ok(deliveryService.update(id, dto));
    }

    @PatchMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> updateNotes(
            @PathVariable Long id,
            @RequestBody @Valid DeliveryNotesRequest request) {
        return ResponseEntity.ok(deliveryService.updateNotes(id, request.getNotes()));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        deliveryService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteByTaskId(@PathVariable Long taskId) {
        deliveryService.deleteByTaskId(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grouped")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<DeliveryGroupResponse>> listGroupedByTask(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> allParams
    ) {
        List<String> sortParams = allParams != null ? allParams.get("sort") : null;
        Pageable pageable = PageRequest.of(page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id"));
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTask(
                taskId, taskName, taskCode, flowType, status, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(deliveryGroups));
    }

    @GetMapping("/grouped-by-task")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<DeliveryGroupResponse>> listGroupedByTaskAlias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> allParams
    ) {
        List<String> sortParams = allParams != null ? allParams.get("sort") : null;
        Pageable pageable = PageRequest.of(page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id"));
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTask(
                taskId, taskName, taskCode, flowType, status, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(deliveryGroups));
    }

    @GetMapping("/group/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryGroupResponse> getGroupDetails(@PathVariable Long taskId) {
        DeliveryGroupResponse groupDetails = deliveryService.findGroupDetailsByTaskId(taskId);
        return ResponseEntity.ok(groupDetails);
    }

    // Novos endpoints otimizados para performance
    @GetMapping("/grouped/optimized")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<DeliveryGroupResponse>> listGroupedByTaskOptimized(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> allParams
    ) {
        List<String> sortParams = allParams != null ? allParams.get("sort") : null;
        Pageable pageable = PageRequest.of(page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id"));
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTaskOptimized(
                taskName, taskCode, status, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(deliveryGroups));
    }

    @GetMapping("/group/{taskId}/optimized")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryGroupResponse> getGroupDetailsOptimized(@PathVariable Long taskId) {
        DeliveryGroupResponse groupDetails = deliveryService.findGroupDetailsByTaskIdOptimized(taskId);
        return ResponseEntity.ok(groupDetails);
    }

    @GetMapping("/by-task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> getByTaskId(@PathVariable Long taskId) {
        DeliveryResponse delivery = deliveryService.findByTaskId(taskId);
        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(delivery);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryStatusCount> getGlobalStatistics() {
        DeliveryStatusCount statistics = deliveryService.getGlobalStatistics();
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/update-all-statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateAllDeliveryStatuses() {
        deliveryService.updateAllDeliveryStatuses();
        return ResponseEntity.ok("Status de todas as entregas foram atualizados");
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportDeliveriesToExcel(
            @RequestParam(required = false) String flowType) throws IOException {
        byte[] excelData = deliveryService.exportToExcel(flowType);
        
        String filename = "relatorio_entregas_" + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")) + 
                         ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/send-delivery-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> sendDeliveryEmail(
            @PathVariable Long id,
            @RequestBody(required = false) br.com.devquote.dto.request.SendFinancialEmailRequest request
    ) {
        try {
            java.util.List<String> additionalEmails = request != null && request.getAdditionalEmails() != null
                    ? request.getAdditionalEmails()
                    : new java.util.ArrayList<>();
            deliveryService.sendDeliveryEmail(id, additionalEmails);
            return ResponseEntity.ok("Email de entrega enviado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao enviar email: " + e.getMessage());
        }
    }
}
