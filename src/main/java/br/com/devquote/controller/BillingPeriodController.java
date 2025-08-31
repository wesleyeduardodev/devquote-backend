package br.com.devquote.controller;
import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.service.BillingPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/billing-periods")
@Validated
@RequiredArgsConstructor
public class BillingPeriodController {

    private final BillingPeriodService billingPeriodService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodResponse>> list() {
        return ResponseEntity.ok(billingPeriodService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingPeriodService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodResponse> create(@RequestBody @Valid BillingPeriodRequest dto) {
        return new ResponseEntity<>(billingPeriodService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodResponse> update(@PathVariable Long id, @RequestBody @Valid BillingPeriodRequest dto) {
        return ResponseEntity.ok(billingPeriodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingPeriodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        billingPeriodService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<BillingPeriodResponse>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(billingPeriodService.findAllPaginated(
            month, year, status, pageable));
    }

    @GetMapping("/with-totals")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingPeriodResponse>> listWithTotals() {
        return ResponseEntity.ok(billingPeriodService.findAllWithTotals());
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(billingPeriodService.getStatistics());
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status) throws IOException {
        
        byte[] excelData = billingPeriodService.exportToExcel(month, year, status);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "relatorio-faturamento.xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete-with-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteWithAllLinkedTasks(@PathVariable Long id) {
        billingPeriodService.deleteWithAllLinkedTasks(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingPeriodResponse> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        return ResponseEntity.ok(billingPeriodService.updateStatus(id, status));
    }
}