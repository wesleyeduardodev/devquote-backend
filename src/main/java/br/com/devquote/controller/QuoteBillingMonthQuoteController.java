package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteBillingMonthQuoteControllerDoc;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import br.com.devquote.service.QuoteBillingMonthQuoteService;
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
@RequestMapping("/api/quote-billing-month-quotes")
@Validated
@RequiredArgsConstructor
public class QuoteBillingMonthQuoteController implements QuoteBillingMonthQuoteControllerDoc {

    private final QuoteBillingMonthQuoteService service;

    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> create(@RequestBody @Valid QuoteBillingMonthQuoteRequest dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> update(@PathVariable Long id, @RequestBody @Valid QuoteBillingMonthQuoteRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        service.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-billing-month/{billingMonthId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponse>> getByBillingMonth(
            @PathVariable Long billingMonthId) {
        return ResponseEntity.ok(service.findByQuoteBillingMonthId(billingMonthId));
    }

    @GetMapping("/by-billing-month/{billingMonthId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<QuoteBillingMonthQuoteResponse>> getByBillingMonthPaginated(
            @PathVariable Long billingMonthId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(service.findByQuoteBillingMonthIdPaginated(billingMonthId, pageable));
    }

    @PostMapping("/bulk-link")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponse>> bulkLink(
            @RequestBody @Valid List<QuoteBillingMonthQuoteRequest> requests) {
        return new ResponseEntity<>(service.bulkCreate(requests), HttpStatus.CREATED);
    }

    @DeleteMapping("/by-billing-month/{billingMonthId}/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> bulkUnlinkFromBillingMonth(
            @PathVariable Long billingMonthId,
            @RequestBody List<Long> quoteIds) {
        service.bulkUnlinkByBillingMonthAndQuoteIds(billingMonthId, quoteIds);
        return ResponseEntity.noContent().build();
    }
}
