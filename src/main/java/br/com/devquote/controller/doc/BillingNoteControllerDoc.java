package br.com.devquote.controller.doc;

import br.com.devquote.dto.request.BillingNoteRequest;
import br.com.devquote.dto.response.BillingNoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "Billing Notes")
public interface BillingNoteControllerDoc {

    @Operation(summary = "Lista as anotações gerais de faturamento (sem período vinculado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anotações retornadas com sucesso")
    })
    ResponseEntity<List<BillingNoteResponse>> getGeneralNotes();

    @Operation(summary = "Lista as anotações de um período de faturamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anotações retornadas com sucesso")
    })
    ResponseEntity<List<BillingNoteResponse>> getNotesByBillingPeriod(
            @Parameter(description = "ID do período de faturamento", required = true) @PathVariable Long billingPeriodId);

    @Operation(summary = "Contagem de anotações por escopo (chave 'general' e IDs de período)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagens retornadas com sucesso")
    })
    ResponseEntity<Map<String, Long>> countNotes();

    @Operation(summary = "Busca uma anotação pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anotação encontrada"),
            @ApiResponse(responseCode = "404", description = "Anotação não encontrada")
    })
    ResponseEntity<BillingNoteResponse> getById(
            @Parameter(description = "ID da anotação", required = true) @PathVariable Long id);

    @Operation(summary = "Cria uma anotação de faturamento (geral ou de período)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anotação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<BillingNoteResponse> create(@RequestBody BillingNoteRequest request);

    @Operation(summary = "Atualiza uma anotação de faturamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anotação atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Anotação não encontrada")
    })
    ResponseEntity<BillingNoteResponse> update(
            @Parameter(description = "ID da anotação", required = true) @PathVariable Long id,
            @RequestBody BillingNoteRequest request);

    @Operation(summary = "Exclui uma anotação de faturamento e seus anexos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anotação excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Anotação não encontrada")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "ID da anotação", required = true) @PathVariable Long id);
}
