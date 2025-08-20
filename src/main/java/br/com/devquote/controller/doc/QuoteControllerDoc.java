package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.QuoteRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.QuoteResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Quotes")
public interface QuoteControllerDoc {

    @Operation(summary = "List quotes with pagination, multi-field sorting and search")
    @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated quotes")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by quote ID", example = "123")
    @Parameter(name = "taskId", description = "Filter by task ID", example = "456")
    @Parameter(name = "taskName", description = "Filter by task title (partial match)", example = "Landing page")
    @Parameter(name = "taskCode", description = "Filter by task code (partial match)", example = "TASK-001")
    @Parameter(name = "status", description = "Filter by quote status (partial match)", example = "OPEN")
    @Parameter(name = "createdAt", description = "Filter by creation date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01")
    @Parameter(name = "updatedAt", description = "Filter by update date. Supports partial dates", example = "2024-01-15")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,taskId,taskName,taskCode,status,totalAmount,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<QuoteResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Get quote by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of quote"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<QuoteResponse> getById(
            @Parameter(description = "Quote id", required = true) Long id);

    @Operation(summary = "Create a new quote")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quote created successfully")
    })
    ResponseEntity<QuoteResponse> create(
            @Parameter(description = "Quote payload", required = true) @Valid QuoteRequest dto);

    @Operation(summary = "Update an existing quote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quote updated successfully"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<QuoteResponse> update(
            @Parameter(description = "Quote id", required = true) Long id,
            @Parameter(description = "Quote payload", required = true) @Valid QuoteRequest dto);

    @Operation(summary = "Delete a quote")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quote deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Quote id", required = true) Long id);
}
