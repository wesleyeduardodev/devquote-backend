package br.com.devquote.controller;
import br.com.devquote.controller.doc.TaskControllerDoc;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;
import br.com.devquote.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Validated
@RequiredArgsConstructor
public class TaskController implements TaskControllerDoc {

    private final TaskService taskService;

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskResponseDTO>> list() {
        return ResponseEntity.ok(taskService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<TaskResponseDTO> create(@RequestBody @Valid TaskRequestDTO dto) {
        return new ResponseEntity<>(taskService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable Long id, @RequestBody @Valid TaskRequestDTO dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/full")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<TaskWithSubTasksResponseDTO> createWithSubTasks(
            @RequestBody @Valid TaskWithSubTasksRequestDTO dto) {
        return new ResponseEntity<>(taskService.createWithSubTasks(dto), HttpStatus.CREATED);
    }

    @PutMapping("/full/{taskId}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<TaskWithSubTasksResponseDTO> updateWithSubTasks(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskWithSubTasksUpdateRequestDTO dto) {
        return ResponseEntity.ok(taskService.updateWithSubTasks(taskId, dto));
    }

    @DeleteMapping("/full/{taskId}")
    @PreAuthorize("hasAuthority('SCOPE_admin:users')")
    public ResponseEntity<Void> deleteTaskWithSubTasks(@PathVariable Long taskId) {
        taskService.deleteTaskWithSubTasks(taskId);
        return ResponseEntity.noContent().build();
    }
}