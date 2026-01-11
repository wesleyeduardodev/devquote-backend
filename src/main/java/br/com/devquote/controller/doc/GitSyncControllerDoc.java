package br.com.devquote.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Git Sync", description = "Sincronizacao de PRs com provedores Git")
public interface GitSyncControllerDoc {

    @Operation(summary = "Executar sincronizacao de PRs mergeados",
            description = "Verifica todos os DeliveryItems com PR pendente e atualiza o status para PRODUCTION se o PR foi mergeado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronizacao executada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<Map<String, Object>> syncMergedPullRequests();

    @Operation(summary = "Verificar status de PR de um DeliveryItem especifico",
            description = "Verifica se o PR de um DeliveryItem foi mergeado e atualiza o status se necessario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificacao executada com sucesso"),
            @ApiResponse(responseCode = "404", description = "DeliveryItem nao encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    ResponseEntity<Map<String, Object>> checkDeliveryItemPullRequest(
            @Parameter(description = "ID do DeliveryItem") Long deliveryItemId);
}
