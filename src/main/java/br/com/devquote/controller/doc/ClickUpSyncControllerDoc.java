package br.com.devquote.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "ClickUp Sync", description = "Sincronizacao de status de entregas com ClickUp")
public interface ClickUpSyncControllerDoc {

    @Operation(summary = "Executar sincronizacao de entregas com ClickUp",
            description = "Sincroniza o status de todas as entregas elegiveis com as tarefas correspondentes no ClickUp")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronizacao executada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<Map<String, Object>> syncDeliveriesToClickUp();

    @Operation(summary = "Sincronizar uma entrega especifica com ClickUp",
            description = "Sincroniza o status de uma entrega especifica com a tarefa correspondente no ClickUp")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronizacao executada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Entrega nao encontrada"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<Map<String, Object>> syncDeliveryToClickUp(
            @Parameter(description = "ID da Delivery") Long deliveryId);
}
