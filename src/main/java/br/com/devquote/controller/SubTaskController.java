package br.com.devquote.controller;
import br.com.devquote.controller.doc.SubTaskControllerDoc;
import br.com.devquote.dto.request.SubTaskRequestDTO;
import br.com.devquote.dto.response.SubTaskResponseDTO;
import br.com.devquote.service.SubTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sub-tasks")
@Validated
@RequiredArgsConstructor
public class SubTaskController implements SubTaskControllerDoc {

    private final SubTaskService subTaskService;

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SubTaskResponseDTO>> list() {
        return ResponseEntity.ok(subTaskService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subTaskService.findById(id));
    }

    @Override
    @PostMapping
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponseDTO> create(@RequestBody @Valid SubTaskRequestDTO dto) {
        return new ResponseEntity<>(subTaskService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponseDTO> update(@PathVariable Long id, @RequestBody @Valid SubTaskRequestDTO dto) {
        return ResponseEntity.ok(subTaskService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_admin:users')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}