package br.com.devquote.controller;

import br.com.devquote.dto.request.BoardPreferencesRequest;
import br.com.devquote.dto.response.PriorityBoardResponse;
import br.com.devquote.service.PriorityBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/priorities")
@RequiredArgsConstructor
@Slf4j
public class PriorityController {

    private final PriorityBoardService priorityBoardService;

    @GetMapping("/board")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriorityBoardResponse> getBoard(
            @RequestParam(name = "includeAssignee", defaultValue = "true") boolean includeAssignee) {
        return ResponseEntity.ok(priorityBoardService.getBoard(includeAssignee));
    }

    @PostMapping("/board/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriorityBoardResponse> refresh(
            @RequestParam(name = "includeAssignee", defaultValue = "true") boolean includeAssignee) {
        priorityBoardService.evict();
        return ResponseEntity.ok(priorityBoardService.getBoard(includeAssignee));
    }

    /**
     * Atualiza preferências do board (ordem dos grupos, status principal, status ocultos).
     * Atualização parcial — manda só o que mudou.
     */
    @PutMapping("/board/preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePreferences(@RequestBody BoardPreferencesRequest request) {
        priorityBoardService.updatePreferences(request);
        return ResponseEntity.noContent().build();
    }
}
