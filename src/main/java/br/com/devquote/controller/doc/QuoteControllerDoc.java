package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Quotes")
public interface QuoteControllerDoc {

    @Operation(summary = "List all quotes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of quotes")
    })
    ResponseEntity<List<QuoteResponseDTO>> list();

    @Operation(summary = "Get quote by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of quote"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<QuoteResponseDTO> getById(
            @Parameter(description = "Quote id", required = true) Long id);

    @Operation(summary = "Create a new quote")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quote created successfully")
    })
    ResponseEntity<QuoteResponseDTO> create(
            @Parameter(description = "Quote payload", required = true) @Valid QuoteRequestDTO dto);

    @Operation(summary = "Update an existing quote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quote updated successfully"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<QuoteResponseDTO> update(
            @Parameter(description = "Quote id", required = true) Long id,
            @Parameter(description = "Quote payload", required = true) @Valid QuoteRequestDTO dto);

    @Operation(summary = "Delete a quote")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quote deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Quote id", required = true) Long id);
}
