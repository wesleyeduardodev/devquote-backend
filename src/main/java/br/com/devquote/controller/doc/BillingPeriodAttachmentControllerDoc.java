package br.com.devquote.controller.doc;

import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Billing Period Attachments")
public interface BillingPeriodAttachmentControllerDoc {

    @Operation(summary = "Upload multiple files to a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error uploading files")
    })
    ResponseEntity<List<BillingPeriodAttachmentResponse>> uploadFiles(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId,
            @Parameter(description = "Files to upload", required = true) @RequestParam("files") List<MultipartFile> files);

    @Operation(summary = "Upload a single file to a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error uploading file")
    })
    ResponseEntity<BillingPeriodAttachmentResponse> uploadFile(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId,
            @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file);

    @Operation(summary = "Get all attachments for a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Error retrieving attachments")
    })
    ResponseEntity<List<BillingPeriodAttachmentResponse>> getBillingPeriodAttachments(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId);

    @Operation(summary = "Get attachment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment found"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Error retrieving attachment")
    })
    ResponseEntity<BillingPeriodAttachmentResponse> getAttachmentById(
            @Parameter(description = "Attachment ID", required = true) @PathVariable Long attachmentId);

    @Operation(summary = "Download attachment file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Error downloading file")
    })
    ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable Long attachmentId);

    @Operation(summary = "Delete an attachment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "500", description = "Error deleting attachment")
    })
    ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment ID", required = true) @PathVariable Long attachmentId);

    @Operation(summary = "Delete multiple attachments")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attachments deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Error deleting attachments")
    })
    ResponseEntity<Void> deleteAttachments(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Attachment IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> attachmentIds);

    @Operation(summary = "Delete all attachments for a billing period")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All attachments deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Billing period not found"),
            @ApiResponse(responseCode = "500", description = "Error deleting attachments")
    })
    ResponseEntity<Void> deleteAllBillingPeriodAttachments(
            @Parameter(description = "Billing period ID", required = true) @PathVariable Long billingPeriodId);
}
