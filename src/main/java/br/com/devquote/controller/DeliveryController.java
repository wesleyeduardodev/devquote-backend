package br.com.devquote.controller;
import br.com.devquote.controller.doc.DeliveryControllerDoc;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import br.com.devquote.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@Validated
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDoc {

    private final DeliveryService deliveryService;

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeliveryResponseDTO>> list() {
        return ResponseEntity.ok(deliveryService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<DeliveryResponseDTO> create(@RequestBody @Valid DeliveryRequestDTO dto) {
        return new ResponseEntity<>(deliveryService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<DeliveryResponseDTO> update(@PathVariable Long id, @RequestBody @Valid DeliveryRequestDTO dto) {
        return ResponseEntity.ok(deliveryService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}