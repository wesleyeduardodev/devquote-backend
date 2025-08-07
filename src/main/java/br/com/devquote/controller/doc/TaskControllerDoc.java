package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Tasks")
public interface TaskControllerDoc {

    @Operation(summary = "List all tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of tasks")
    })
    ResponseEntity<List<TaskResponseDTO>> list();

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
}
