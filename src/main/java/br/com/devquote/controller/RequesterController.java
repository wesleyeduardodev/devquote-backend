package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.RequesterControllerDoc;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.PagedResponseDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import br.com.devquote.service.RequesterService;
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
@RequestMapping("/api/requesters")
@Validated
@RequiredArgsConstructor
public class RequesterController implements RequesterControllerDoc {

    private final RequesterService requesterService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "email", "phone", "createdAt", "updatedAt"
    );

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<RequesterResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {

        List<String> sortParams = params.get("sort");

        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );

        Page<RequesterResponseDTO> pageResult = requesterService.findAllPaginated(
                id, name, email, phone, createdAt, updatedAt, pageable
        );

        PagedResponseDTO<RequesterResponseDTO> response = PageAdapter.toPagedResponseDTO(pageResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequesterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(requesterService.findById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequesterResponseDTO> create(@RequestBody @Valid RequesterRequestDTO dto) {
        return new ResponseEntity<>(requesterService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequesterResponseDTO> update(@PathVariable Long id, @RequestBody @Valid RequesterRequestDTO dto) {
        return ResponseEntity.ok(requesterService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requesterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
