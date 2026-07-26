package br.com.devquote.controller;

import br.com.devquote.controller.doc.BillingNoteControllerDoc;
import br.com.devquote.dto.request.BillingNoteRequest;
import br.com.devquote.dto.response.BillingNoteResponse;
import br.com.devquote.service.BillingNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing-notes")
@RequiredArgsConstructor
@Slf4j
public class BillingNoteController implements BillingNoteControllerDoc {

    private final BillingNoteService billingNoteService;

    @GetMapping("/general")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingNoteResponse>> getGeneralNotes() {
        return ResponseEntity.ok(billingNoteService.getGeneralNotes());
    }

    @GetMapping("/billing-period/{billingPeriodId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BillingNoteResponse>> getNotesByBillingPeriod(@PathVariable Long billingPeriodId) {
        return ResponseEntity.ok(billingNoteService.getNotesByBillingPeriod(billingPeriodId));
    }

    @GetMapping("/counts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> countNotes() {
        return ResponseEntity.ok(billingNoteService.countNotes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BillingNoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(billingNoteService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingNoteResponse> create(@Valid @RequestBody BillingNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingNoteService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillingNoteResponse> update(@PathVariable Long id, @Valid @RequestBody BillingNoteRequest request) {
        return ResponseEntity.ok(billingNoteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
