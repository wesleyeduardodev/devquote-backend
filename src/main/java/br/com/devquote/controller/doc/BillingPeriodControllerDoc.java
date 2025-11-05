package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Tag(name = "Billing Periods")
public interface BillingPeriodControllerDoc {

    @Operation(summary = "List billing periods with filters")
    @ApiResponse(responseCode = "200", description = "Billing periods retrieved successfully")
    @Parameter(name = "year", description = "Filter by year", example = "2024")
    @Parameter(name = "month", description = "Filter by month (1-12)", example = "11")
    @Parameter(name = "status", description = "Filter by status: PENDING, SENT, PAID, CANCELLED", example = "PENDING")
    @Parameter(name = "flowType", description = "Filter by flow type: TODOS, DESENVOLVIMENTO, OPERACIONAL", example = "DESENVOLVIMENTO")
    ResponseEntity<List<BillingPeriodResponse>> list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String flowType);

    @Operation(summary = "Get billing period by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billing period found"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<BillingPeriodResponse> getById(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id);

    @Operation(summary = "Create a new billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Billing period created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or month/year already exists")
    })
    ResponseEntity<BillingPeriodResponse> create(
            @Parameter(description = "Billing period data", required = true) @RequestBody @Valid BillingPeriodRequest dto);

    @Operation(summary = "Update an existing billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billing period updated successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<BillingPeriodResponse> update(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id,
            @Parameter(description = "Billing period data", required = true) @RequestBody @Valid BillingPeriodRequest dto);

    @Operation(summary = "Delete a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Billing period deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id);

    @Operation(summary = "Delete multiple billing periods")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Billing periods deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Billing Period IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> ids);

    @Operation(summary = "Get billing periods with pagination")
    @ApiResponse(responseCode = "200", description = "Paginated billing periods retrieved successfully")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "sortBy", description = "Field to sort by", example = "id")
    @Parameter(name = "sortDirection", description = "Sort direction: asc or desc", example = "desc")
    @Parameter(name = "month", description = "Filter by month (1-12)", example = "11")
    @Parameter(name = "year", description = "Filter by year", example = "2024")
    @Parameter(name = "status", description = "Filter by status", example = "PENDING")
    ResponseEntity<Page<BillingPeriodResponse>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status);

    @Operation(summary = "List billing periods with calculated totals")
    @ApiResponse(responseCode = "200", description = "Billing periods with totals retrieved successfully")
    ResponseEntity<List<BillingPeriodResponse>> listWithTotals();

    @Operation(summary = "Get billing statistics")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    ResponseEntity<?> getStatistics();

    @Operation(summary = "Export billing periods to Excel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
            @ApiResponse(responseCode = "500", description = "Error generating Excel file")
    })
    @Parameter(name = "month", description = "Filter by month", example = "11")
    @Parameter(name = "year", description = "Filter by year", example = "2024")
    @Parameter(name = "status", description = "Filter by status", example = "PENDING")
    @Parameter(name = "flowType", description = "Filter by flow type", example = "DESENVOLVIMENTO")
    ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String flowType) throws IOException;

    @Operation(summary = "Delete billing period and all linked tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Billing period and tasks deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<Void> deleteWithAllLinkedTasks(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id);

    @Operation(summary = "Update billing period status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<BillingPeriodResponse> updateStatus(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id,
            @Parameter(description = "New status: PENDING, SENT, PAID, CANCELLED", required = true) @RequestParam String status);

    @Operation(summary = "Send billing email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<Void> sendBillingEmail(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long id,
            @Parameter(description = "Email request with additional recipients") @RequestBody(required = false) br.com.devquote.dto.request.SendFinancialEmailRequest request);
}
