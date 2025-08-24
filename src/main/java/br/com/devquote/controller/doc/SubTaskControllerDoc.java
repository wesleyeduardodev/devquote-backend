package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.SubTaskRequest;
import br.com.devquote.dto.response.SubTaskResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
    ResponseEntity<List<SubTaskResponse>> list();

    @Operation(summary = "Get sub task by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of sub task"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<SubTaskResponse> getById(
            @Parameter(description = "SubTask id", required = true) Long id);

    @Operation(summary = "Create a new sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SubTask created successfully")
    })
    ResponseEntity<SubTaskResponse> create(
            @Parameter(description = "SubTask payload", required = true) @Valid SubTaskRequest dto);

    @Operation(summary = "Update an existing sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SubTask updated successfully"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<SubTaskResponse> update(
            @Parameter(description = "SubTask id", required = true) Long id,
            @Parameter(description = "SubTask payload", required = true) @Valid SubTaskRequest dto);

    @Operation(summary = "Delete a sub task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SubTask deleted successfully"),
            @ApiResponse(responseCode = "404", description = "SubTask not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "SubTask id", required = true) Long id);

    @Operation(summary = "Delete multiple sub-tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sub-tasks deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @RequestBody(
                    description = "List of SubTask IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            List<Long> ids
    );
}
