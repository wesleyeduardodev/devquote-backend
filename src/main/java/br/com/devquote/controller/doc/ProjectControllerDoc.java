package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.ProjectRequestDTO;
import br.com.devquote.dto.response.ProjectResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Projects")
public interface ProjectControllerDoc {

    @Operation(summary = "List all projects")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of projects")
    })
    ResponseEntity<List<ProjectResponseDTO>> list();

    @Operation(summary = "Get project by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<ProjectResponseDTO> getById(
            @Parameter(description = "Project id", required = true) Long id);

    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully")
    })
    ResponseEntity<ProjectResponseDTO> create(
            @Parameter(description = "Project payload", required = true) ProjectRequestDTO dto);

    @Operation(summary = "Update an existing project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<ProjectResponseDTO> update(
            @Parameter(description = "Project id", required = true) Long id,
            @Parameter(description = "Project payload", required = true) ProjectRequestDTO dto);

    @Operation(summary = "Delete a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Project id", required = true) Long id);
}
