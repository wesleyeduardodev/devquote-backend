package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.RequesterRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.RequesterResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Requesters")
public interface RequesterControllerDoc {

    @Operation(summary = "List requesters with pagination, multi-field sorting and search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated requesters"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by requester ID", example = "123")
    @Parameter(name = "name", description = "Filter by requester name (partial match)", example = "John")
    @Parameter(name = "email", description = "Filter by requester email (partial match)", example = "john@example.com")
    @Parameter(name = "phone", description = "Filter by requester phone (partial match)", example = "+1234567890")
    @Parameter(name = "createdAt", description = "Filter by creation date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01")
    @Parameter(name = "updatedAt", description = "Filter by update date. Supports partial dates: '2024', '2024-01', '2024-01-15'", example = "2024-01-15")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,name,email,phone,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<RequesterResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam MultiValueMap<String, String> params
    );

    @Operation(summary = "Get requester by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of requester"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<RequesterResponse> getById(
            @Parameter(description = "Requester id", required = true) Long id);

    @Operation(summary = "Create a new requester")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Requester created successfully")
    })
    ResponseEntity<RequesterResponse> create(
            @Parameter(description = "Requester payload", required = true) @Valid RequesterRequest dto);

    @Operation(summary = "Update an existing requester")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requester updated successfully"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<RequesterResponse> update(
            @Parameter(description = "Requester id", required = true) Long id,
            @Parameter(description = "Requester payload", required = true) @Valid RequesterRequest dto);

    @Operation(summary = "Delete a requester")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Requester deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Requester id", required = true) Long id);

    @Operation(summary = "Delete multiple requesters")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Requesters deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @RequestBody(
                    description = "List of Requester IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            List<Long> ids
    );
}
