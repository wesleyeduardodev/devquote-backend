package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.ProjectRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Projects")
public interface ProjectControllerDoc {

    @Operation(summary = "List projects with pagination, multi-field sorting and search")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated projects")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by project ID", example = "123")
    @Parameter(name = "name", description = "Filter by project name (partial match)", example = "DevQuote")
    @Parameter(name = "repositoryUrl", description = "Filter by repository URL (partial match)", example = "github.com/user/repo")
    @Parameter(name = "createdAt", description = "Filter by creation date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01")
    @Parameter(name = "updatedAt", description = "Filter by update date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01-15")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,name,repositoryUrl,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<ProjectResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String repositoryUrl,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Get project by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<ProjectResponse> getById(
            @Parameter(description = "Project id", required = true) Long id);

    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully")
    })
    ResponseEntity<ProjectResponse> create(
            @Parameter(description = "Project payload", required = true) @Valid ProjectRequest dto);

    @Operation(summary = "Update an existing project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<ProjectResponse> update(
            @Parameter(description = "Project id", required = true) Long id,
            @Parameter(description = "Project payload", required = true) @Valid ProjectRequest dto);

    @Operation(summary = "Delete a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Project id", required = true) Long id);
}
