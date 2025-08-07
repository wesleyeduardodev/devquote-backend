package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.SubTaskRequestDTO;
import br.com.devquote.dto.response.SubTaskResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "SubTasks")
public interface SubTaskControllerDoc {

    @Operation(summary = "List all sub tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of sub tasks")
    })
    ResponseEntity<List<SubTaskResponseDTO>> list();

    @Operation(summary = "Get sub task by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of sub task"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<SubTaskResponseDTO> getById(
            @Parameter(description = "SubTask id", required = true) Long id);

    @Operation(summary = "Create a new sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SubTask created successfully")
    })
    ResponseEntity<SubTaskResponseDTO> create(
            @Parameter(description = "SubTask payload", required = true) @Valid SubTaskRequestDTO dto);

    @Operation(summary = "Update an existing sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SubTask updated successfully"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<SubTaskResponseDTO> update(
            @Parameter(description = "SubTask id", required = true) Long id,
            @Parameter(description = "SubTask payload", required = true) @Valid SubTaskRequestDTO dto);

    @Operation(summary = "Delete a sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SubTask deleted successfully"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "SubTask id", required = true) Long id);
}
