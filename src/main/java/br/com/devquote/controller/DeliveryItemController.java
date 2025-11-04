package br.com.devquote.controller;

import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.dto.request.DeliveryItemRequest;
import br.com.devquote.dto.response.DeliveryItemResponse;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.service.DeliveryItemService;
import br.com.devquote.utils.SortUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/delivery-items")
@Validated
@RequiredArgsConstructor
@Tag(name = "DeliveryItem Management", description = "Operations for managing delivery items")
public class DeliveryItemController {

    private final DeliveryItemService deliveryItemService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "deliveryId", "taskId", "taskName", "taskCode", "projectName",
            "branch", "sourceBranch", "pullRequest", "status", "startedAt", "finishedAt", "createdAt", "updatedAt"
    );

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "List delivery items with pagination and filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery items retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PagedResponse<DeliveryItemResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by delivery item ID")
            @RequestParam(required = false) Long id,
            @Parameter(description = "Filter by delivery ID")
            @RequestParam(required = false) Long deliveryId,
            @Parameter(description = "Filter by task ID")
            @RequestParam(required = false) Long taskId,
            @Parameter(description = "Filter by task name")
            @RequestParam(required = false) String taskName,
            @Parameter(description = "Filter by task code")
            @RequestParam(required = false) String taskCode,
            @Parameter(description = "Filter by project name")
            @RequestParam(required = false) String projectName,
            @Parameter(description = "Filter by branch")
            @RequestParam(required = false) String branch,
            @Parameter(description = "Filter by pull request")
            @RequestParam(required = false) String pullRequest,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) DeliveryStatus status,
            @Parameter(description = "Filter by started date")
            @RequestParam(required = false) String startedAt,
            @Parameter(description = "Filter by finished date")
            @RequestParam(required = false) String finishedAt,
            @Parameter(description = "Filter by creation date")
            @RequestParam(required = false) String createdAt,
            @Parameter(description = "Filter by update date")
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {
        List<String> sortParams = params != null ? params.get("sort") : null;
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );
        
        Page<DeliveryItemResponse> itemsPage = deliveryItemService.findAllPaginated(
                id, deliveryId, taskId, taskName, taskCode, projectName, branch, pullRequest,
                status, startedAt, finishedAt, createdAt, updatedAt, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(itemsPage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery item found"),
            @ApiResponse(responseCode = "404", description = "Delivery item not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<DeliveryItemResponse> findById(
            @Parameter(description = "Delivery item ID") @PathVariable Long id
    ) {
        DeliveryItemResponse item = deliveryItemService.findById(id);
        return ResponseEntity.ok(item);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create new delivery item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Delivery item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<DeliveryItemResponse> create(
            @Parameter(description = "Delivery item data") @Valid @RequestBody DeliveryItemRequest request
    ) {
        DeliveryItemResponse item = deliveryItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update delivery item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<DeliveryItemResponse> update(
            @Parameter(description = "Delivery item ID") @PathVariable Long id,
            @Parameter(description = "Updated delivery item data") @Valid @RequestBody DeliveryItemRequest request
    ) {
        DeliveryItemResponse item = deliveryItemService.update(id, request);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete delivery item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delivery item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery item not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Delivery item ID") @PathVariable Long id
    ) {
        deliveryItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Bulk delete delivery items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delivery items deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteBulk(
            @Parameter(description = "List of delivery item IDs to delete") @RequestBody List<Long> ids
    ) {
        deliveryItemService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-delivery/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery items by delivery ID")
    public ResponseEntity<List<DeliveryItemResponse>> findByDeliveryId(
            @Parameter(description = "Delivery ID") @PathVariable Long deliveryId
    ) {
        List<DeliveryItemResponse> items = deliveryItemService.findByDeliveryId(deliveryId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery items by task ID")
    public ResponseEntity<List<DeliveryItemResponse>> findByTaskId(
            @Parameter(description = "Task ID") @PathVariable Long taskId
    ) {
        List<DeliveryItemResponse> items = deliveryItemService.findByTaskId(taskId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-task/{taskId}/optimized")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery items by task ID (optimized query)")
    public ResponseEntity<List<DeliveryItemResponse>> findByTaskIdOptimized(
            @Parameter(description = "Task ID") @PathVariable Long taskId
    ) {
        List<DeliveryItemResponse> items = deliveryItemService.findItemsByTaskIdOptimized(taskId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery items by project ID")
    public ResponseEntity<List<DeliveryItemResponse>> findByProjectId(
            @Parameter(description = "Project ID") @PathVariable Long projectId
    ) {
        List<DeliveryItemResponse> items = deliveryItemService.findByProjectId(projectId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get delivery items by status")
    public ResponseEntity<List<DeliveryItemResponse>> findByStatus(
            @Parameter(description = "Delivery status") @PathVariable DeliveryStatus status
    ) {
        List<DeliveryItemResponse> items = deliveryItemService.findByStatus(status);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/delivery/{deliveryId}/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create multiple delivery items for a delivery")
    public ResponseEntity<List<DeliveryItemResponse>> createMultipleItems(
            @Parameter(description = "Delivery ID") @PathVariable Long deliveryId,
            @Parameter(description = "List of delivery items") @Valid @RequestBody List<DeliveryItemRequest> items
    ) {
        List<DeliveryItemResponse> createdItems = deliveryItemService.createMultipleItems(deliveryId, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItems);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update multiple delivery items")
    public ResponseEntity<List<DeliveryItemResponse>> updateMultipleItems(
            @Parameter(description = "List of delivery item IDs") @RequestParam List<Long> itemIds,
            @Parameter(description = "List of updated delivery items") @Valid @RequestBody List<DeliveryItemRequest> items
    ) {
        List<DeliveryItemResponse> updatedItems = deliveryItemService.updateMultipleItems(itemIds, items);
        return ResponseEntity.ok(updatedItems);
    }

    @GetMapping("/count/by-delivery/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Count delivery items by delivery ID")
    public ResponseEntity<Long> countByDeliveryId(
            @Parameter(description = "Delivery ID") @PathVariable Long deliveryId
    ) {
        long count = deliveryItemService.countByDeliveryId(deliveryId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/by-delivery/{deliveryId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Count delivery items by delivery ID and status")
    public ResponseEntity<Long> countByDeliveryIdAndStatus(
            @Parameter(description = "Delivery ID") @PathVariable Long deliveryId,
            @Parameter(description = "Delivery status") @PathVariable DeliveryStatus status
    ) {
        long count = deliveryItemService.countByDeliveryIdAndStatus(deliveryId, status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Export delivery items to Excel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export successful"),
            @ApiResponse(responseCode = "500", description = "Export failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        byte[] excelData = deliveryItemService.exportToExcel();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            "delivery_items_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}