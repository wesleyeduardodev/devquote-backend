package br.com.devquote.controller;

import br.com.devquote.controller.doc.BillingPeriodTaskControllerDoc;
import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import br.com.devquote.enums.FlowType;
import br.com.devquote.service.BillingPeriodTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing-period-tasks")
@Validated
@RequiredArgsConstructor
public class BillingPeriodTaskController implements BillingPeriodTaskControllerDoc {

    private final BillingPeriodTaskService billingPeriodTaskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodTaskResponse>> list() {
        return ResponseEntity.ok(billingPeriodTaskService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodTaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingPeriodTaskService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodTaskResponse> create(@RequestBody @Valid BillingPeriodTaskRequest dto) {
        return new ResponseEntity<>(billingPeriodTaskService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodTaskResponse> update(@PathVariable Long id, @RequestBody @Valid BillingPeriodTaskRequest dto) {
        return ResponseEntity.ok(billingPeriodTaskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingPeriodTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        billingPeriodTaskService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/billing-period/{billingPeriodId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodTaskResponse>> findByBillingPeriod(
            @PathVariable Long billingPeriodId,
            @RequestParam(required = false) String flowType) {

        FlowType flowTypeEnum = (flowType == null || flowType.equals("TODOS"))
            ? null
            : FlowType.fromString(flowType);

        return ResponseEntity.ok(billingPeriodTaskService.findByBillingPeriodAndFlowType(billingPeriodId, flowTypeEnum));
    }

    @GetMapping("/billing-period/{billingPeriodId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<BillingPeriodTaskResponse>> findByBillingPeriodPaginated(
            @PathVariable Long billingPeriodId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String flowType) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        FlowType flowTypeEnum = (flowType == null || flowType.equals("TODOS"))
            ? null
            : FlowType.fromString(flowType);

        return ResponseEntity.ok(billingPeriodTaskService.findByBillingPeriodPaginated(billingPeriodId, pageable, flowTypeEnum));
    }

    @PostMapping("/bulk-link")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodTaskResponse>> bulkLink(@RequestBody List<BillingPeriodTaskRequest> requests) {
        return ResponseEntity.ok(billingPeriodTaskService.bulkCreate(requests));
    }

    @DeleteMapping("/billing-period/{billingPeriodId}/bulk-unlink")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> bulkUnlink(
            @PathVariable Long billingPeriodId,
            @RequestBody List<Long> taskIds) {
        billingPeriodTaskService.bulkUnlinkTasks(billingPeriodId, taskIds);
        return ResponseEntity.noContent().build();
    }
}