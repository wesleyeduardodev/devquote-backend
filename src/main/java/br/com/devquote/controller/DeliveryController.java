package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.DeliveryControllerDoc;
import br.com.devquote.dto.request.DeliveryEnvironmentRequest;
import br.com.devquote.dto.request.DeliveryNotesRequest;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.DeliveryStatusCount;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.service.DeliveryService;
import br.com.devquote.service.DeliveryAttachmentService;
import br.com.devquote.utils.SecurityUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/deliveries")
@Validated
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDoc {

    private final DeliveryService deliveryService;
    private final DeliveryAttachmentService deliveryAttachmentService;
    private final SecurityUtils securityUtils;

    /** Remove o valor da tarefa (taskValue) quando o usuário é USER. */
    private DeliveryResponse sanitizeAmounts(DeliveryResponse d) {
        if (d != null && securityUtils.cannotViewMonetaryValues()) d.setTaskValue(null);
        return d;
    }

    private Page<DeliveryResponse> sanitizeAmounts(Page<DeliveryResponse> page) {
        if (page != null && securityUtils.cannotViewMonetaryValues()) {
            page.getContent().forEach(d -> { if (d != null) d.setTaskValue(null); });
        }
        return page;
    }

    private DeliveryGroupResponse sanitizeAmounts(DeliveryGroupResponse g) {
        if (g != null && securityUtils.cannotViewMonetaryValues()) {
            g.setTaskValue(null);
            if (g.getDeliveries() != null) {
                g.getDeliveries().forEach(d -> { if (d != null) d.setTaskValue(null); });
            }
        }
        return g;
    }

    private Page<DeliveryGroupResponse> sanitizeGroupAmounts(Page<DeliveryGroupResponse> page) {
        if (page != null && securityUtils.cannotViewMonetaryValues()) {
            page.getContent().forEach(this::sanitizeAmounts);
        }
        return page;
    }

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
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) Boolean hasItems,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {
        List<String> sortParams = params != null ? params.get("sort") : null;

        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );

        Page<DeliveryResponse> pageResult = deliveryService.findAllPaginated(
                id, taskId, taskName, taskCode, flowType, taskType, environment, status,
                startDate, endDate, createdAt, updatedAt, hasItems, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(sanitizeAmounts(pageResult)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<br.com.devquote.dto.response.DeliveryStats> getStats() {
        return ResponseEntity.ok(deliveryService.getStats());
    }

    @GetMapping("/total-amount")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<java.util.Map<String, java.math.BigDecimal>> getTotalAmount(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) Boolean hasItems
    ) {
        if (securityUtils.cannotViewMonetaryValues()) {
            return ResponseEntity.ok(java.util.Map.of("totalAmount", java.math.BigDecimal.ZERO));
        }
        java.math.BigDecimal totalAmount = deliveryService.getTotalAmount(
                id, taskId, taskName, taskCode, flowType, taskType, environment, status,
                startDate, endDate, createdAt, updatedAt, hasItems
        );
        return ResponseEntity.ok(java.util.Map.of("totalAmount", totalAmount));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sanitizeAmounts(deliveryService.findById(id)));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> create(@RequestBody @Valid DeliveryRequest dto) {
        return new ResponseEntity<>(deliveryService.create(dto), HttpStatus.CREATED);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> createWithFiles(
            @RequestParam("dto") String dtoJson,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        try {

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            DeliveryRequest deliveryRequest = objectMapper.readValue(dtoJson, DeliveryRequest.class);

            DeliveryResponse delivery = deliveryService.create(deliveryRequest);

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> update(@PathVariable Long id, @RequestBody @Valid DeliveryRequest dto) {
        return ResponseEntity.ok(deliveryService.update(id, dto));
    }

    @PatchMapping("/{id}/notes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> updateNotes(
            @PathVariable Long id,
            @RequestBody @Valid DeliveryNotesRequest request) {
        return ResponseEntity.ok(deliveryService.updateNotes(id, request.getNotes()));
    }

    @PatchMapping("/{id}/environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponse> updateEnvironment(
            @PathVariable Long id,
            @RequestBody @Valid DeliveryEnvironmentRequest request) {
        return ResponseEntity.ok(deliveryService.updateEnvironment(id, request.getEnvironment()));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
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
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> allParams
    ) {
        List<String> sortParams = allParams != null ? allParams.get("sort") : null;
        Pageable pageable = PageRequest.of(page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id"));
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTask(
                taskId, taskName, taskCode, flowType, taskType, environment, status, startDate, endDate, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(sanitizeGroupAmounts(deliveryGroups)));
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
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> allParams
    ) {
        List<String> sortParams = allParams != null ? allParams.get("sort") : null;
        Pageable pageable = PageRequest.of(page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id"));
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTask(
                taskId, taskName, taskCode, flowType, taskType, environment, status, startDate, endDate, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(sanitizeGroupAmounts(deliveryGroups)));
    }

    @GetMapping("/group/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryGroupResponse> getGroupDetails(@PathVariable Long taskId) {
        DeliveryGroupResponse groupDetails = deliveryService.findGroupDetailsByTaskId(taskId);
        return ResponseEntity.ok(sanitizeAmounts(groupDetails));
    }

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
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(sanitizeGroupAmounts(deliveryGroups)));
    }

    @GetMapping("/group/{taskId}/optimized")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryGroupResponse> getGroupDetailsOptimized(@PathVariable Long taskId) {
        DeliveryGroupResponse groupDetails = deliveryService.findGroupDetailsByTaskIdOptimized(taskId);
        return ResponseEntity.ok(sanitizeAmounts(groupDetails));
    }

    @GetMapping("/by-task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> getByTaskId(@PathVariable Long taskId) {
        DeliveryResponse delivery = deliveryService.findByTaskId(taskId);
        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sanitizeAmounts(delivery));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryStatusCount> getGlobalStatistics(
            @RequestParam(required = false) String flowType) {
        DeliveryStatusCount statistics;
        if (flowType != null && !flowType.isBlank()) {
            statistics = deliveryService.getStatisticsByFlowType(flowType);
        } else {
            statistics = deliveryService.getGlobalStatistics();
        }
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
            @RequestParam(required = false) String flowType,
            @RequestParam(required = false, defaultValue = "false") boolean canViewAmounts) throws IOException {
        // Enforce server-side: USER nunca vê valores, independentemente do parâmetro do cliente.
        boolean showAmounts = securityUtils.canViewMonetaryValues();
        byte[] excelData = deliveryService.exportToExcel(flowType, showAmounts);

        String filename = "relatorio_entregas_" +
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")) +
                         ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/export/excel-only")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportDeliveriesOnlyToExcel(
            @RequestParam(required = false, defaultValue = "false") boolean canViewAmounts) throws IOException {
        // Enforce server-side: USER nunca vê valores, independentemente do parâmetro do cliente.
        boolean showAmounts = securityUtils.canViewMonetaryValues();
        byte[] excelData = deliveryService.exportDeliveriesOnlyToExcel(showAmounts);

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
            List<String> additionalEmails = request != null && request.getAdditionalEmails() != null
                    ? request.getAdditionalEmails()
                    : new ArrayList<>();

           List<String> additionalWhatsAppRecipients = request != null && request.getAdditionalWhatsAppRecipients() != null
                    ? request.getAdditionalWhatsAppRecipients()
                    : new ArrayList<>();

            boolean sendEmail = request != null && request.getSendEmail() != null
                    ? request.getSendEmail()
                    : true;

            boolean sendWhatsApp = request != null && request.getSendWhatsApp() != null
                    ? request.getSendWhatsApp()
                    : true;

            deliveryService.sendDeliveryEmail(id, additionalEmails, additionalWhatsAppRecipients, sendEmail, sendWhatsApp);
            return ResponseEntity.ok("Notificação de entrega enviada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao enviar notificação: " + e.getMessage());
        }
    }
}
