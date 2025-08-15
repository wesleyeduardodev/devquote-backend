package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.PagedResponseDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Requesters")
public interface RequesterControllerDoc {

    @Operation(summary = "List requesters with pagination, sorting and search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of paginated requesters"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Number of items per page", example = "10"),
            @Parameter(name = "sortBy", description = "Field to sort by", example = "name"),
            @Parameter(name = "sortDir", description = "Sort direction", example = "asc"),
            @Parameter(name = "search", description = "Search term for name or email", example = "João")
    })
    ResponseEntity<PagedResponseDTO<RequesterResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    );

    @Operation(summary = "Get requester by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of requester"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<RequesterResponseDTO> getById(
            @Parameter(description = "Requester id", required = true) Long id);

    @Operation(summary = "Create a new requester")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Requester created successfully")
    })
    ResponseEntity<RequesterResponseDTO> create(
            @Parameter(description = "Requester payload", required = true) @Valid RequesterRequestDTO dto);

    @Operation(summary = "Update an existing requester")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requester updated successfully"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<RequesterResponseDTO> update(
            @Parameter(description = "Requester id", required = true) Long id,
            @Parameter(description = "Requester payload", required = true) @Valid RequesterRequestDTO dto);

    @Operation(summary = "Delete a requester")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Requester deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Requester not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Requester id", required = true) Long id);
}
