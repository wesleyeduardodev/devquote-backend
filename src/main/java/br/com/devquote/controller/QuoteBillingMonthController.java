package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteBillingMonthControllerDoc;
import br.com.devquote.dto.request.QuoteBillingMonthRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthResponseDTO;
import br.com.devquote.service.QuoteBillingMonthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quote-billing-months")
@Validated
@RequiredArgsConstructor
public class QuoteBillingMonthController implements QuoteBillingMonthControllerDoc {

    private final QuoteBillingMonthService quoteBillingMonthService;

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuoteBillingMonthResponseDTO>> list() {
        return ResponseEntity.ok(quoteBillingMonthService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteBillingMonthService.findById(id));
    }

    @Override
    @PostMapping
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthResponseDTO> create(@RequestBody @Valid QuoteBillingMonthRequestDTO dto) {
        return new ResponseEntity<>(quoteBillingMonthService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteBillingMonthResponseDTO> update(@PathVariable Long id, @RequestBody @Valid QuoteBillingMonthRequestDTO dto) {
        return ResponseEntity.ok(quoteBillingMonthService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteBillingMonthService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
