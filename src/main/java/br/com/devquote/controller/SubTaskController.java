package br.com.devquote.controller;
import br.com.devquote.controller.doc.SubTaskControllerDoc;
import br.com.devquote.dto.request.SubTaskRequest;
import br.com.devquote.dto.response.SubTaskResponse;
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
    public ResponseEntity<List<SubTaskResponse>> list() {
        return ResponseEntity.ok(subTaskService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subTaskService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponse> create(@RequestBody @Valid SubTaskRequest dto) {
        return new ResponseEntity<>(subTaskService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubTaskResponse> update(@PathVariable Long id, @RequestBody @Valid SubTaskRequest dto) {
        return ResponseEntity.ok(subTaskService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        subTaskService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
}
