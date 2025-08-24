package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Deliveries")
public interface DeliveryControllerDoc {

    @Operation(summary = "List deliveries with pagination, multi-field sorting and search")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated deliveries")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by delivery ID", example = "123")
    @Parameter(name = "quoteId", description = "Filter by quote ID", example = "456")
    @Parameter(name = "taskId", description = "Filter by task ID", example = "789")
    @Parameter(name = "taskName", description = "Filter by task title (partial)", example = "Landing page")
    @Parameter(name = "taskCode", description = "Filter by task code (partial)", example = "TASK-001")
    @Parameter(name = "projectId", description = "Filter by project ID", example = "33")
    @Parameter(name = "projectName", description = "Filter by project name (partial)", example = "DevQuote")
    @Parameter(name = "branch", description = "Filter by branch (partial)", example = "feature/")
    @Parameter(name = "pullRequest", description = "Filter by pull request URL (partial)", example = "github.com")
    @Parameter(name = "status", description = "Filter by status (partial)", example = "DONE")
    @Parameter(name = "startedAt", description = "Filter by start date (supports partial: '2025', '2025-08', '2025-08-01')", example = "2025-08")
    @Parameter(name = "finishedAt", description = "Filter by finish date (supports partial)", example = "2025-08-19")
    @Parameter(name = "createdAt", description = "Filter by creation date (supports partial)", example = "2025-08")
    @Parameter(name = "updatedAt", description = "Filter by update date (supports partial)", example = "2025-08-19")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,quoteId,taskId,taskName,taskCode,projectId,projectName,branch,pullRequest,status,startedAt,finishedAt,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<DeliveryResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String pullRequest,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startedAt,
            @RequestParam(required = false) String finishedAt,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Get delivery by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of delivery"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<DeliveryResponse> getById(
            @Parameter(description = "Delivery id", required = true) Long id);

    @Operation(summary = "Create a new delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Delivery created successfully")
    })
    ResponseEntity<DeliveryResponse> create(
            @Parameter(description = "Delivery payload", required = true) @Valid DeliveryRequest dto);

    @Operation(summary = "Update an existing delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<DeliveryResponse> update(
            @Parameter(description = "Delivery id", required = true) Long id,
            @Parameter(description = "Delivery payload", required = true) @Valid DeliveryRequest dto);

    @Operation(summary = "Delete a delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Delivery deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Delivery id", required = true) Long id);

    @Operation(summary = "Delete multiple deliveries")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deliveries deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @RequestBody(
                    description = "List of Delivery IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            List<Long> ids
    );
}
