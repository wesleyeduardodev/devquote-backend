package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.DeliveryOperationalItemRequest;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.dto.response.DeliveryOperationalItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Delivery Operational Items")
public interface DeliveryOperationalControllerDoc {

    @Operation(summary = "Create a new operational delivery item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operational item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<DeliveryOperationalItemResponse> createItem(
            @Parameter(description = "Operational item data", required = true) @RequestBody @Valid DeliveryOperationalItemRequest request);

    @Operation(summary = "Update an operational delivery item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operational item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Operational item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<DeliveryOperationalItemResponse> updateItem(
            @Parameter(description = "Operational item ID", required = true) @PathVariable Long id,
            @Parameter(description = "Operational item data", required = true) @RequestBody @Valid DeliveryOperationalItemRequest request);

    @Operation(summary = "Get operational delivery item by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operational item found"),
            @ApiResponse(responseCode = "404", description = "Operational item not found")
    })
    ResponseEntity<DeliveryOperationalItemResponse> getItem(
            @Parameter(description = "Operational item ID", required = true) @PathVariable Long id);

    @Operation(summary = "Get operational items by delivery ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operational items retrieved successfully")
    })
    ResponseEntity<List<DeliveryOperationalItemResponse>> getItemsByDelivery(
            @Parameter(description = "Delivery ID", required = true) @PathVariable Long deliveryId);

    @Operation(summary = "Delete an operational delivery item")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operational item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Operational item not found")
    })
    ResponseEntity<Void> deleteItem(
            @Parameter(description = "Operational item ID", required = true) @PathVariable Long id);

    @Operation(summary = "Upload attachments to an operational item")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attachments uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error uploading attachments")
    })
    ResponseEntity<List<DeliveryOperationalAttachmentResponse>> uploadAttachments(
            @Parameter(description = "Operational item ID", required = true) @PathVariable Long itemId,
            @Parameter(description = "Files to upload", required = true) @RequestParam("files") List<MultipartFile> files) throws IOException;

    @Operation(summary = "Get attachments for an operational item")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully")
    })
    ResponseEntity<List<DeliveryOperationalAttachmentResponse>> getAttachments(
            @Parameter(description = "Operational item ID", required = true) @PathVariable Long itemId);

    @Operation(summary = "Download an operational item attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Error downloading attachment")
    })
    ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable Long id) throws IOException;

    @Operation(summary = "Delete an operational item attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Error deleting attachment")
    })
    ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable Long id) throws IOException;
}
