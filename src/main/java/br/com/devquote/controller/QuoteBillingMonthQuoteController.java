package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteBillingMonthQuoteControllerDoc;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import br.com.devquote.service.QuoteBillingMonthQuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    @PostMapping
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> create(@RequestBody @Valid QuoteBillingMonthQuoteRequest dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthQuoteResponse> update(@PathVariable Long id, @RequestBody @Valid QuoteBillingMonthQuoteRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-billing-month/{billingMonthId}")
    //@PreAuthorize("isAuthenticated()")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponse>> getByBillingMonth(
            @PathVariable Long billingMonthId) {
        return ResponseEntity.ok(service.findByQuoteBillingMonthId(billingMonthId));
    }
}