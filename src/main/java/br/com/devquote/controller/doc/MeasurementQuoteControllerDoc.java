package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.MeasurementQuoteRequestDTO;
import br.com.devquote.dto.response.MeasurementQuoteResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "MeasurementQuotes")
public interface MeasurementQuoteControllerDoc {

    @Operation(summary = "List all measurement-quote relations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of measurement-quote relations")
    })
    ResponseEntity<List<MeasurementQuoteResponseDTO>> list();

    @Operation(summary = "Get measurement-quote relation by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of measurement-quote relation"),
            @ApiResponse(responseCode = "404", description = "MeasurementQuote not found")
    })
    ResponseEntity<MeasurementQuoteResponseDTO> getById(
            @Parameter(description = "MeasurementQuote id", required = true) Long id);

    @Operation(summary = "Create a new measurement-quote relation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "MeasurementQuote created successfully")
    })
    ResponseEntity<MeasurementQuoteResponseDTO> create(
            @Parameter(description = "MeasurementQuote payload", required = true) MeasurementQuoteRequestDTO dto);

    @Operation(summary = "Update an existing measurement-quote relation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MeasurementQuote updated successfully"),
            @ApiResponse(responseCode = "404", description = "MeasurementQuote not found")
    })
    ResponseEntity<MeasurementQuoteResponseDTO> update(
            @Parameter(description = "MeasurementQuote id", required = true) Long id,
            @Parameter(description = "MeasurementQuote payload", required = true) MeasurementQuoteRequestDTO dto);

    @Operation(summary = "Delete a measurement-quote relation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "MeasurementQuote deleted successfully"),
            @ApiResponse(responseCode = "404", description = "MeasurementQuote not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "MeasurementQuote id", required = true) Long id);
}
