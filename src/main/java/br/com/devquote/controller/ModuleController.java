package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.ModuleControllerDoc;
import br.com.devquote.dto.request.ModuleRequest;
import br.com.devquote.dto.response.ModuleResponse;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.service.ModuleService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/modules")
@Validated
@RequiredArgsConstructor
public class ModuleController implements ModuleControllerDoc {

    private final ModuleService moduleService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "createdAt", "updatedAt"
    );

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<ModuleResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {
        List<String> sortParams = params != null ? params.get("sort") : null;
        Pageable pageable = PageRequest.of(
                page, size, SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "name")
        );
        Page<ModuleResponse> pageResult = moduleService.findAllPaginated(id, name, createdAt, updatedAt, pageable);
        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(pageResult));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> create(@RequestBody @Valid ModuleRequest dto) {
        return new ResponseEntity<>(moduleService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> update(@PathVariable Long id, @RequestBody @Valid ModuleRequest dto) {
        return ResponseEntity.ok(moduleService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        moduleService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
}
