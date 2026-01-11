package br.com.devquote.controller;

import br.com.devquote.controller.doc.GitSyncControllerDoc;
import br.com.devquote.service.GitPullRequestSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/git-sync")
@RequiredArgsConstructor
@Slf4j
public class GitSyncController implements GitSyncControllerDoc {

    private final GitPullRequestSyncService gitPullRequestSyncService;

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Map<String, Object>> syncMergedPullRequests() {
        log.info("Sincronizacao manual de PRs iniciada via API");

        long startTime = System.currentTimeMillis();

        try {
            gitPullRequestSyncService.syncMergedPullRequests();

            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sincronizacao executada com sucesso");
            response.put("durationMs", duration);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro na sincronizacao manual: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erro na sincronizacao: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/check/{deliveryItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Override
    public ResponseEntity<Map<String, Object>> checkDeliveryItemPullRequest(
            @PathVariable Long deliveryItemId) {
        log.info("Verificacao manual de PR para DeliveryItem {} iniciada via API", deliveryItemId);

        try {
            boolean updated = gitPullRequestSyncService.checkAndUpdatePullRequestStatus(deliveryItemId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deliveryItemId", deliveryItemId);
            response.put("updated", updated);
            response.put("message", updated
                    ? "PR mergeado detectado - status atualizado para PRODUCTION"
                    : "PR ainda nao foi mergeado ou integracao desabilitada");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao verificar PR do DeliveryItem {}: {}", deliveryItemId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("deliveryItemId", deliveryItemId);
            response.put("message", "Erro: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
