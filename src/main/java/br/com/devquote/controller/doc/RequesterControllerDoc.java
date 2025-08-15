package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.PagedResponseDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
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
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,name,email,phone,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponseDTO<RequesterResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam MultiValueMap<String, String> params
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
