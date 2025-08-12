package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteBillingMonthQuoteControllerDoc;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponseDTO;
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
    public ResponseEntity<List<QuoteBillingMonthQuoteResponseDTO>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthQuoteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<QuoteBillingMonthQuoteResponseDTO> create(@RequestBody @Valid QuoteBillingMonthQuoteRequestDTO dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<QuoteBillingMonthQuoteResponseDTO> update(@PathVariable Long id, @RequestBody @Valid QuoteBillingMonthQuoteRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-billing-month/{billingMonthId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuoteBillingMonthQuoteResponseDTO>> getByBillingMonth(
            @PathVariable Long billingMonthId) {
        return ResponseEntity.ok(service.findByQuoteBillingMonthId(billingMonthId));
    }
}