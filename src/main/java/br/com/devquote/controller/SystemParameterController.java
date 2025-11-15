package br.com.devquote.controller;

import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.SystemParameterResponse;
import br.com.devquote.service.SystemParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system-parameters")
@RequiredArgsConstructor
@Tag(name = "System Parameters", description = "API para gerenciamento de parâmetros do sistema")
public class SystemParameterController {

    private final SystemParameterService systemParameterService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os parâmetros (sem paginação)")
    public ResponseEntity<List<SystemParameterResponse>> findAll() {
        return ResponseEntity.ok(systemParameterService.findAll());
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar parâmetros com paginação e filtros")
    public ResponseEntity<PagedResponse<SystemParameterResponse>> findAllPaginated(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort.Direction direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<SystemParameterResponse> pageResult = systemParameterService.findAllPaginated(
                id, name, description, createdAt, updatedAt, pageable
        );

        PagedResponse<SystemParameterResponse> response = PageAdapter.toPagedResponseDTO(pageResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar parâmetro por ID")
    public ResponseEntity<SystemParameterResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(systemParameterService.findById(id));
    }

    @GetMapping("/by-name")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar parâmetro por nome")
    public ResponseEntity<SystemParameterResponse> findByName(@RequestParam String name) {
        return ResponseEntity.ok(systemParameterService.findByName(name));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo parâmetro")
    public ResponseEntity<SystemParameterResponse> create(@Valid @RequestBody SystemParameterRequest request) {
        SystemParameterResponse response = systemParameterService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar parâmetro existente")
    public ResponseEntity<SystemParameterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SystemParameterRequest request) {
        return ResponseEntity.ok(systemParameterService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir parâmetro por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        systemParameterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir múltiplos parâmetros")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        systemParameterService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
}
