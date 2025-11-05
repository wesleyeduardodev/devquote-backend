package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
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

import java.util.List;

@Tag(name = "Billing Period Tasks")
public interface BillingPeriodTaskControllerDoc {

    @Operation(summary = "List all billing period tasks")
    @ApiResponse(responseCode = "200", description = "Billing period tasks retrieved successfully")
    ResponseEntity<List<BillingPeriodTaskResponse>> list();

    @Operation(summary = "Get billing period task by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billing period task found"),
            @ApiResponse(responseCode = "404", description = "Billing period task not found")
    })
    ResponseEntity<BillingPeriodTaskResponse> getById(
            @Parameter(description = "Billing period task ID", required = true) @PathVariable Long id);

    @Operation(summary = "Create a new billing period task link")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Billing period task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<BillingPeriodTaskResponse> create(
            @Parameter(description = "Billing period task data", required = true) @RequestBody @Valid BillingPeriodTaskRequest dto);

    @Operation(summary = "Update an existing billing period task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Billing period task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period task not found")
    })
    ResponseEntity<BillingPeriodTaskResponse> update(
            @Parameter(description = "Billing period task ID", required = true) @PathVariable Long id,
            @Parameter(description = "Billing period task data", required = true) @RequestBody @Valid BillingPeriodTaskRequest dto);

    @Operation(summary = "Delete a billing period task")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Billing period task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period task not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Billing period task ID", required = true) @PathVariable Long id);

    @Operation(summary = "Delete multiple billing period tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Billing period tasks deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Billing Period Task IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> ids);

    @Operation(summary = "Get tasks by billing period ID")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    @Parameter(name = "flowType", description = "Filter by flow type: TODOS, DESENVOLVIMENTO, OPERACIONAL", example = "DESENVOLVIMENTO")
    ResponseEntity<List<BillingPeriodTaskResponse>> findByBillingPeriod(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId,
            @RequestParam(required = false) String flowType);

    @Operation(summary = "Get tasks by billing period ID with pagination")
    @ApiResponse(responseCode = "200", description = "Paginated tasks retrieved successfully")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "sortBy", description = "Field to sort by", example = "id")
    @Parameter(name = "sortDirection", description = "Sort direction: asc or desc", example = "desc")
    @Parameter(name = "flowType", description = "Filter by flow type", example = "DESENVOLVIMENTO")
    ResponseEntity<Page<BillingPeriodTaskResponse>> findByBillingPeriodPaginated(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String flowType);

    @Operation(summary = "Link multiple tasks to a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks linked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<List<BillingPeriodTaskResponse>> bulkLink(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of billing period task requests",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BillingPeriodTaskRequest.class)))
            )
            @RequestBody List<BillingPeriodTaskRequest> requests);

    @Operation(summary = "Unlink multiple tasks from a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tasks unlinked successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found")
    })
    ResponseEntity<Void> bulkUnlink(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Task IDs to unlink",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> taskIds);
}
