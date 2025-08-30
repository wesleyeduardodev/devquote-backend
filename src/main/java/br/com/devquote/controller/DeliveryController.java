package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.DeliveryControllerDoc;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.service.DeliveryService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/deliveries")
@Validated
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDoc {

    private final DeliveryService deliveryService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "taskName", "taskCode", "projectName",
            "branch", "pullRequest", "status", "startedAt", "finishedAt", "createdAt", "updatedAt"
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
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String pullRequest,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startedAt,
            @RequestParam(required = false) String finishedAt,
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
                id, taskName, taskCode, projectName,
                branch, pullRequest, status, startedAt, finishedAt, createdAt, updatedAt, pageable
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

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryResponse> update(@PathVariable Long id, @RequestBody @Valid DeliveryRequest dto) {
        return ResponseEntity.ok(deliveryService.update(id, dto));
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
    
    @DeleteMapping("/quote/{quoteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteByQuoteId(@PathVariable Long quoteId) {
        deliveryService.deleteByQuoteId(quoteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grouped")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<DeliveryGroupResponse>> listGroupedByTask(
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
        Page<DeliveryGroupResponse> deliveryGroups = deliveryService.findAllGroupedByTask(
                taskName, taskCode, status, createdAt, updatedAt, pageable
        );
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(deliveryGroups));
    }

    @GetMapping("/group/{quoteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeliveryGroupResponse> getGroupDetails(@PathVariable Long quoteId) {
        DeliveryGroupResponse groupDetails = deliveryService.findGroupDetailsByQuoteId(quoteId);
        return ResponseEntity.ok(groupDetails);
    }
}
