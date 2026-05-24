package br.com.devquote.controller;

import br.com.devquote.dto.request.ClickUpSetupSaveRequest;
import br.com.devquote.dto.request.ClickUpSetupValidateRequest;
import br.com.devquote.dto.response.ClickUpSetupFieldsResponse;
import br.com.devquote.dto.response.ClickUpSetupItemResponse;
import br.com.devquote.dto.response.ClickUpSetupUserResponse;
import br.com.devquote.service.ClickUpSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wizard de setup do ClickUp.
 *
 * Todas as rotas exigem ADMIN. O TOKEN é passado em cada request (header ou body) e
 * NÃO é persistido até o user clicar "Salvar" no passo final (POST /save).
 *
 * Permite navegar pela hierarquia do ClickUp (workspaces → spaces → lists → fields)
 * pra o user escolher tudo via dropdown, sem precisar copiar IDs manualmente.
 */
@RestController
@RequestMapping("/api/integrations/clickup/setup")
@RequiredArgsConstructor
public class ClickUpSetupController {

    private static final String AUTH_HEADER = "X-ClickUp-Token";

    private final ClickUpSetupService service;

    @PostMapping("/validate-token")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClickUpSetupUserResponse> validateToken(@RequestBody @Valid ClickUpSetupValidateRequest request) {
        return ResponseEntity.ok(service.validateToken(request.getToken()));
    }

    @GetMapping("/workspaces")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClickUpSetupItemResponse>> listWorkspaces(@RequestHeader(AUTH_HEADER) String token) {
        return ResponseEntity.ok(service.listTeams(token));
    }

    @GetMapping("/workspaces/{teamId}/spaces")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClickUpSetupItemResponse>> listSpaces(
            @RequestHeader(AUTH_HEADER) String token,
            @PathVariable String teamId) {
        return ResponseEntity.ok(service.listSpaces(token, teamId));
    }

    @GetMapping("/spaces/{spaceId}/lists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClickUpSetupItemResponse>> listLists(
            @RequestHeader(AUTH_HEADER) String token,
            @PathVariable String spaceId) {
        return ResponseEntity.ok(service.listLists(token, spaceId));
    }

    @GetMapping("/workspaces/{teamId}/shared-lists")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClickUpSetupItemResponse>> listSharedLists(
            @RequestHeader(AUTH_HEADER) String token,
            @PathVariable String teamId) {
        return ResponseEntity.ok(service.listSharedLists(token, teamId));
    }

    @GetMapping("/lists/{listId}/fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClickUpSetupFieldsResponse> listFields(
            @RequestHeader(AUTH_HEADER) String token,
            @PathVariable String listId) {
        return ResponseEntity.ok(service.listFields(token, listId));
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> save(@RequestBody @Valid ClickUpSetupSaveRequest request) {
        service.save(request);
        return ResponseEntity.noContent().build();
    }

    /** Apaga os parâmetros do núcleo (token, listId, devField, devOption, orderField, enabled).
     *  Preferências do board (ordem/principal/ocultos) são preservadas. */
    @org.springframework.web.bind.annotation.DeleteMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reset() {
        service.reset();
        return ResponseEntity.noContent().build();
    }
}
