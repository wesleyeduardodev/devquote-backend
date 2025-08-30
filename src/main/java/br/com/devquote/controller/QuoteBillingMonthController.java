package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteBillingMonthControllerDoc;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.service.QuoteBillingMonthService;
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
@RequestMapping("/api/quote-billing-months")
@Validated
@RequiredArgsConstructor
public class QuoteBillingMonthController implements QuoteBillingMonthControllerDoc {

    private final QuoteBillingMonthService quoteBillingMonthService;

    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<QuoteBillingMonthResponse>> list() {
        return ResponseEntity.ok(quoteBillingMonthService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteBillingMonthService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthResponse> create(@RequestBody @Valid QuoteBillingMonthRequest dto) {
        return new ResponseEntity<>(quoteBillingMonthService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthResponse> update(@PathVariable Long id, @RequestBody @Valid QuoteBillingMonthRequest dto) {
        return ResponseEntity.ok(quoteBillingMonthService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteBillingMonthService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        quoteBillingMonthService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<QuoteBillingMonthResponse>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(quoteBillingMonthService.findAllPaginated(
            month, year, status, pageable));
    }

    @GetMapping("/with-totals")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<QuoteBillingMonthResponse>> listWithTotals() {
        return ResponseEntity.ok(quoteBillingMonthService.findAllWithTotals());
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(quoteBillingMonthService.getStatistics());
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status) throws IOException {
        
        byte[] excelData = quoteBillingMonthService.exportToExcel(month, year, status);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "relatorio-faturamento.xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}
