package br.com.devquote.controller.doc;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Quote Billing Month - Quote link")
public interface QuoteBillingMonthQuoteControllerDoc {

    @Operation(summary = "List all links between quote billing month and quotes")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successful retrieval"))
    ResponseEntity<List<QuoteBillingMonthQuoteResponse>> list();

    @Operation(summary = "Get link by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<QuoteBillingMonthQuoteResponse> getById(Long id);

    @Operation(summary = "Create a new link")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Created"))
    ResponseEntity<QuoteBillingMonthQuoteResponse> create(@Valid QuoteBillingMonthQuoteRequest dto);

    @Operation(summary = "Update a link (change associations)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<QuoteBillingMonthQuoteResponse> update(Long id, @Valid QuoteBillingMonthQuoteRequest dto);

    @Operation(summary = "Delete a link")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    ResponseEntity<Void> delete(Long id);

    @Operation(summary = "List all links for a specific billing month")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "Billing month not found")
    })
    ResponseEntity<List<QuoteBillingMonthQuoteResponse>> getByBillingMonth(Long billingMonthId);
}