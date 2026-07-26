package br.com.devquote.controller.doc;

import br.com.devquote.dto.response.BillingNoteAttachmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Billing Note Attachments")
public interface BillingNoteAttachmentControllerDoc {

    @Operation(summary = "Envia arquivos para uma anotação de faturamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivos enviados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao enviar arquivos")
    })
    ResponseEntity<List<BillingNoteAttachmentResponse>> uploadFiles(
            @Parameter(description = "ID da anotação", required = true) @PathVariable Long billingNoteId,
            @Parameter(description = "Arquivos a enviar", required = true) @RequestParam("files") List<MultipartFile> files);

    @Operation(summary = "Lista os anexos de uma anotação de faturamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anexos retornados com sucesso")
    })
    ResponseEntity<List<BillingNoteAttachmentResponse>> getBillingNoteAttachments(
            @Parameter(description = "ID da anotação", required = true) @PathVariable Long billingNoteId);

    @Operation(summary = "Faz download de um anexo de anotação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download iniciado"),
            @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "ID do anexo", required = true) @PathVariable Long attachmentId);

    @Operation(summary = "Exclui um anexo de anotação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anexo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "ID do anexo", required = true) @PathVariable Long attachmentId);
}
