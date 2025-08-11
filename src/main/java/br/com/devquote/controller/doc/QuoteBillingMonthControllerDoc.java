package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.QuoteBillingMonthRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Quote Billing Month")
public interface QuoteBillingMonthControllerDoc {

    @Operation(summary = "List all quote billing months")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval")
    })
    ResponseEntity<List<QuoteBillingMonthResponseDTO>> list();

    @Operation(summary = "Get quote billing month by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<QuoteBillingMonthResponseDTO> getById(Long id);

    @Operation(summary = "Create a quote billing month")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created")
    })
    ResponseEntity<QuoteBillingMonthResponseDTO> create(@Valid QuoteBillingMonthRequestDTO dto);

    @Operation(summary = "Update a quote billing month")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<QuoteBillingMonthResponseDTO> update(Long id, @Valid QuoteBillingMonthRequestDTO dto);

    @Operation(summary = "Delete a quote billing month")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<Void> delete(Long id);
}
