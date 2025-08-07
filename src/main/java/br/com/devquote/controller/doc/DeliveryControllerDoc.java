package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Deliveries")
public interface DeliveryControllerDoc {

    @Operation(summary = "List all deliveries")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of deliveries")
    })
    ResponseEntity<List<DeliveryResponseDTO>> list();

    @Operation(summary = "Get delivery by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of delivery"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<DeliveryResponseDTO> getById(
            @Parameter(description = "Delivery id", required = true) Long id);

    @Operation(summary = "Create a new delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Delivery created successfully")
    })
    ResponseEntity<DeliveryResponseDTO> create(
            @Parameter(description = "Delivery payload", required = true) @Valid DeliveryRequestDTO dto);

    @Operation(summary = "Update an existing delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Delivery updated successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<DeliveryResponseDTO> update(
            @Parameter(description = "Delivery id", required = true) Long id,
            @Parameter(description = "Delivery payload", required = true) @Valid DeliveryRequestDTO dto);

    @Operation(summary = "Delete a delivery")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Delivery deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Delivery not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Delivery id", required = true) Long id);
}
