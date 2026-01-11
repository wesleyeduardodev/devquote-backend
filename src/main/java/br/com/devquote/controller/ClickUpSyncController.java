package br.com.devquote.controller;

import br.com.devquote.client.clickup.ClickUpClient;
import br.com.devquote.controller.doc.ClickUpSyncControllerDoc;
import br.com.devquote.service.ClickUpSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clickup-sync")
@RequiredArgsConstructor
@Slf4j
public class ClickUpSyncController implements ClickUpSyncControllerDoc {

    private final ClickUpSyncService clickUpSyncService;
    private final ClickUpClient clickUpClient;

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Map<String, Object>> syncDeliveriesToClickUp() {
        log.info("Sincronizacao manual com ClickUp iniciada via API");

        long startTime = System.currentTimeMillis();

        try {
            clickUpSyncService.syncDeliveriesToClickUp();

            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sincronizacao com ClickUp executada com sucesso");
            response.put("durationMs", duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro na sincronizacao manual com ClickUp: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro na sincronizacao: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/sync/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Override
    public ResponseEntity<Map<String, Object>> syncDeliveryToClickUp(@PathVariable Long deliveryId) {
        log.info("Sincronizacao manual da Delivery {} com ClickUp iniciada via API", deliveryId);

        try {
            boolean synced = clickUpSyncService.syncDeliveryToClickUp(deliveryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deliveryId", deliveryId);
            response.put("synced", synced);
            response.put("message", synced
                    ? "Entrega sincronizada com sucesso no ClickUp"
                    : "Entrega nao foi sincronizada - integracao desabilitada ou status ja atualizado");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao sincronizar Delivery {} com ClickUp: {}", deliveryId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("deliveryId", deliveryId);
            response.put("message", "Erro: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/task/{taskCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getClickUpTask(@PathVariable String taskCode) {
        log.info("Buscando tarefa {} no ClickUp", taskCode);

        try {
            Map<String, Object> task = clickUpClient.getTask(taskCode);

            if (task == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Tarefa nao encontrada");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(task);

        } catch (Exception e) {
            log.error("Erro ao buscar tarefa {} no ClickUp: {}", taskCode, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/task/{taskCode}/statuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAvailableStatuses(@PathVariable String taskCode) {
        log.info("Buscando status disponiveis para tarefa {} no ClickUp", taskCode);

        try {
            java.util.List<String> statuses = clickUpClient.getAvailableStatuses(taskCode);

            Map<String, Object> response = new HashMap<>();
            response.put("taskCode", taskCode);
            response.put("availableStatuses", statuses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar status para tarefa {}: {}", taskCode, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
