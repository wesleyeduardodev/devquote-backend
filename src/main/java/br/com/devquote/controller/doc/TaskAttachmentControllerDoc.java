package br.com.devquote.controller.doc;
import br.com.devquote.dto.response.TaskAttachmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Tag(name = "TaskAttachment", description = "Operações relacionadas aos anexos de tarefas")
public interface TaskAttachmentControllerDoc {

    @Operation(summary = "Upload múltiplos arquivos para uma tarefa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivos enviados com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskAttachmentResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<TaskAttachmentResponse>> uploadFiles(
            @Parameter(description = "ID da tarefa") Long taskId,
            @Parameter(description = "Lista de arquivos para upload") List<MultipartFile> files);

    @Operation(summary = "Upload de um arquivo para uma tarefa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo enviado com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskAttachmentResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<TaskAttachmentResponse> uploadFile(
            @Parameter(description = "ID da tarefa") Long taskId,
            @Parameter(description = "Arquivo para upload") MultipartFile file);

    @Operation(summary = "Lista todos os anexos de uma tarefa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de anexos retornada com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskAttachmentResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<List<TaskAttachmentResponse>> getTaskAttachments(
            @Parameter(description = "ID da tarefa") Long taskId);

    @Operation(summary = "Busca anexo por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anexo encontrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskAttachmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    ResponseEntity<TaskAttachmentResponse> getAttachment(
            @Parameter(description = "ID do anexo") Long attachmentId);

    @Operation(summary = "Download de um anexo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Download realizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Anexo não encontrado")
    })
    ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "ID do anexo") Long attachmentId);

    @Operation(summary = "Excluir um anexo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anexo excluído com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<String> deleteAttachment(
            @Parameter(description = "ID do anexo") Long attachmentId);

    @Operation(summary = "Excluir múltiplos anexos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anexos excluídos com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<String> deleteAttachments(
            @Parameter(description = "Lista de IDs dos anexos") List<Long> attachmentIds);

    @Operation(summary = "Excluir todos os anexos de uma tarefa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Todos os anexos excluídos com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    ResponseEntity<String> deleteAllTaskAttachments(
            @Parameter(description = "ID da tarefa") Long taskId);
}