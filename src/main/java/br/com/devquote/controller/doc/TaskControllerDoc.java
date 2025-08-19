package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequestDTO;
import br.com.devquote.dto.response.PagedResponseDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Tasks")
public interface TaskControllerDoc {

    @Operation(summary = "List tasks with pagination, multi-field sorting and search")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated tasks")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by task ID", example = "123")
    @Parameter(name = "requesterId", description = "Filter by requester ID", example = "45")
    @Parameter(name = "requesterName", description = "Filter by requester Name", example = "Wesley")
    @Parameter(name = "title", description = "Filter by title (partial match)", example = "Landing page")
    @Parameter(name = "description", description = "Filter by description (partial match)", example = "Figma handoff")
    @Parameter(name = "status", description = "Filter by status (partial match)", example = "OPEN")
    @Parameter(name = "code", description = "Filter by code (partial match)", example = "TASK-001")
    @Parameter(name = "link", description = "Filter by link (partial match)", example = "github.com")
    @Parameter(name = "createdAt", description = "Filter by creation date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01")
    @Parameter(name = "updatedAt", description = "Filter by update date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01-15")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,requesterId,title,description,status,code,link,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponseDTO<TaskResponseDTO>> list(
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
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Get task by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of task"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    ResponseEntity<TaskResponseDTO> getById(
            @Parameter(description = "Task id", required = true) Long id);

    @Operation(summary = "Create a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully")
    })
    ResponseEntity<TaskResponseDTO> create(
            @Parameter(description = "Task payload", required = true) @Valid TaskRequestDTO dto);

    @Operation(summary = "Update an existing task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    ResponseEntity<TaskResponseDTO> update(
            @Parameter(description = "Task id", required = true) Long id,
            @Parameter(description = "Task payload", required = true) @Valid TaskRequestDTO dto);

    @Operation(summary = "Delete a task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Task id", required = true) Long id);

    @Operation(summary = "Create task with subtasks")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task with subtasks created successfully")
    })
    ResponseEntity<TaskWithSubTasksResponseDTO> createWithSubTasks(@Valid TaskWithSubTasksRequestDTO dto);

    @Operation(summary = "Update task and its subtasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task with subtasks updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task or SubTask not found")
    })
    ResponseEntity<TaskWithSubTasksResponseDTO> updateWithSubTasks(Long taskId, @Valid TaskWithSubTasksUpdateRequestDTO dto);

    @Operation(summary = "Delete task and its subtasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task and subtasks deleted"),
            @ApiResponse(responseCode = "400", description = "Task is linked to a quote"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    ResponseEntity<Void> deleteTaskWithSubTasks(@PathVariable Long taskId);
}
