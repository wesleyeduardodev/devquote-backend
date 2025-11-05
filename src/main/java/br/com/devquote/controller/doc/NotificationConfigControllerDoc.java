package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.NotificationConfigRequest;
import br.com.devquote.dto.response.NotificationConfigResponse;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Notification Configs")
public interface NotificationConfigControllerDoc {

    @Operation(summary = "List notification configs with pagination, filters and sorting")
    @ApiResponse(responseCode = "200", description = "Notification configs retrieved successfully")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by config ID", example = "1")
    @Parameter(name = "configType", description = "Filter by config type: FINANCIAL or NOTIFICATION")
    @Parameter(name = "notificationType", description = "Filter by notification type: EMAIL or PHONE")
    @Parameter(name = "primaryEmail", description = "Filter by primary email (partial match)")
    @Parameter(name = "createdAt", description = "Filter by creation date. Supports partial dates: '2024', '2024-01', '2024-01-15'")
    @Parameter(name = "updatedAt", description = "Filter by update date. Supports partial dates")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,configType,notificationType,primaryEmail,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<NotificationConfigResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) NotificationConfigType configType,
            @RequestParam(required = false) NotificationType notificationType,
            @RequestParam(required = false) String primaryEmail,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params);

    @Operation(summary = "Get notification config by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification config found"),
            @ApiResponse(responseCode = "404", description = "Notification config not found")
    })
    ResponseEntity<NotificationConfigResponse> getById(
            @Parameter(description = "Notification config ID", required = true) @PathVariable Long id);

    @Operation(summary = "Find notification configs by type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification configs retrieved successfully")
    })
    ResponseEntity<List<NotificationConfigResponse>> findByConfigTypeAndNotificationType(
            @Parameter(description = "Config type: FINANCIAL or NOTIFICATION", required = true) @RequestParam NotificationConfigType configType,
            @Parameter(description = "Notification type: EMAIL or PHONE", required = true) @RequestParam NotificationType notificationType);

    @Operation(summary = "Create a new notification config")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification config created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<NotificationConfigResponse> create(
            @Parameter(description = "Notification config data", required = true) @RequestBody @Valid NotificationConfigRequest dto);

    @Operation(summary = "Update an existing notification config")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification config updated successfully"),
            @ApiResponse(responseCode = "404", description = "Notification config not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<NotificationConfigResponse> update(
            @Parameter(description = "Notification config ID", required = true) @PathVariable Long id,
            @Parameter(description = "Notification config data", required = true) @RequestBody @Valid NotificationConfigRequest dto);

    @Operation(summary = "Delete a notification config")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification config deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification config not found")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "Notification config ID", required = true) @PathVariable Long id);

    @Operation(summary = "Delete multiple notification configs")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification configs deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Notification Config IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> ids);
}
