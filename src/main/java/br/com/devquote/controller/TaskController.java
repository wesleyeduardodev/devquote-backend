package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.TaskControllerDoc;
import br.com.devquote.dto.request.TaskRequest;
import br.com.devquote.dto.request.TaskWithSubTasksCreateRequest;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.TaskResponse;
import br.com.devquote.dto.response.TaskWithSubTasksResponse;
import br.com.devquote.service.TaskService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
@Validated
@RequiredArgsConstructor
public class TaskController implements TaskControllerDoc {

    private final TaskService taskService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "requesterId", "requesterName", "title", "description", "status", "code", "link", "createdAt", "updatedAt"
    );

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TaskResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false) String requesterName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String link,
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

        Page<TaskResponse> pageResult = taskService.findAllPaginated(
                id, requesterId, requesterName, title, description, status, code, link, createdAt, updatedAt, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(pageResult));
    }

    @GetMapping("/unlinked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TaskResponse>> listUnlinked(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false) String requesterName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String link,
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

        Page<TaskResponse> pageResult = taskService.findUnlinkedTasksPaginated(
                id, requesterId, requesterName, title, description, status, code, link, createdAt, updatedAt, pageable
        );

        return ResponseEntity.ok(PageAdapter.toPagedResponseDTO(pageResult));
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskResponse> create(@RequestBody @Valid TaskRequest dto) {
        return new ResponseEntity<>(taskService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @RequestBody @Valid TaskRequest dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        taskService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/full")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskWithSubTasksResponse> createWithSubTasks(
            @RequestBody @Valid TaskWithSubTasksCreateRequest dto) {
        return new ResponseEntity<>(taskService.createWithSubTasks(dto), HttpStatus.CREATED);
    }

    @PutMapping("/full/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TaskWithSubTasksResponse> updateWithSubTasks(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskWithSubTasksUpdateRequest dto) {
        return ResponseEntity.ok(taskService.updateWithSubTasks(taskId, dto));
    }

    @DeleteMapping("/full/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Void> deleteTaskWithSubTasks(@PathVariable Long taskId) {
        taskService.deleteTaskWithSubTasks(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportTasksToExcel() throws IOException {
        byte[] excelData = taskService.exportTasksToExcel();
        
        String filename = "Relatorio_Tarefas_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/export/general-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportGeneralReport() throws IOException {
        byte[] excelData = taskService.exportGeneralReport();
        
        String filename = "Relatorio_Geral_Completo_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/export/general-report-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportGeneralReportForUser() throws IOException {
        byte[] excelData = taskService.exportGeneralReportForUser();
        
        String filename = "Relatorio_Geral_User_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}