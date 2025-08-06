package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Requesters")
public interface RequesterControllerDoc {

    @Operation(summary = "List all requesters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of requesters")
    })
    ResponseEntity<List<RequesterResponseDTO>> list();

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
