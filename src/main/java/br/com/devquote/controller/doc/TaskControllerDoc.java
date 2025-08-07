package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    ResponseEntity<TaskWithSubTasksResponseDTO> updateWithSubTasks(Long taskId, @Valid  TaskWithSubTasksUpdateRequestDTO dto);

    @Operation(summary = "Delete task and its subtasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task and subtasks deleted"),
            @ApiResponse(responseCode = "400", description = "Task is linked to a quote"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    ResponseEntity<Void> deleteTaskWithSubTasks(@PathVariable Long taskId);
}
