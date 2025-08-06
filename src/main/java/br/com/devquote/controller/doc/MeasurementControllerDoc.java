package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.MeasurementRequestDTO;
import br.com.devquote.dto.response.MeasurementResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Measurements")
public interface MeasurementControllerDoc {

    @Operation(summary = "List all measurements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of measurements")
    })
    ResponseEntity<List<MeasurementResponseDTO>> list();

    @Operation(summary = "Get measurement by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of measurement"),
            @ApiResponse(responseCode = "404", description = "Measurement not found")
    })
    ResponseEntity<MeasurementResponseDTO> getById(
            @Parameter(description = "Measurement id", required = true) Long id);

    @Operation(summary = "Create a new measurement")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Measurement created successfully")
    })
    ResponseEntity<MeasurementResponseDTO> create(
            @Parameter(description = "Measurement payload", required = true) MeasurementRequestDTO dto);

    @Operation(summary = "Update an existing measurement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Measurement updated successfully"),
            @ApiResponse(responseCode = "404", description = "Measurement not found")
    })
    ResponseEntity<MeasurementResponseDTO> update(
            @Parameter(description = "Measurement id", required = true) Long id,
            @Parameter(description = "Measurement payload", required = true) MeasurementRequestDTO dto);

    @Operation(summary = "Delete a measurement")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Measurement deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Measurement not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Measurement id", required = true) Long id);
}
