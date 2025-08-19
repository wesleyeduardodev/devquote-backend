package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.QuoteControllerDoc;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.PagedResponseDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import br.com.devquote.service.QuoteService;
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
@RequestMapping("/api/quotes")
@Validated
@RequiredArgsConstructor
public class QuoteController implements QuoteControllerDoc {

    private final QuoteService quoteService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "taskId", "taskName", "taskCode", "status", "createdAt", "updatedAt"
    );

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<QuoteResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {
        List<String> sortParams = params != null ? params.get("sort") : null;

        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );

        Page<QuoteResponseDTO> pageResult = quoteService.findAllPaginated(
                id, taskId, taskName, taskCode, status, createdAt, updatedAt, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(pageResult));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.findById(id));
    }

    @Override
    @PostMapping
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteResponseDTO> create(@RequestBody @Valid QuoteRequestDTO dto) {
        return new ResponseEntity<>(quoteService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuoteResponseDTO> update(@PathVariable Long id, @RequestBody @Valid QuoteRequestDTO dto) {
        return ResponseEntity.ok(quoteService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}