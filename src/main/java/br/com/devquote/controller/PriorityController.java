package br.com.devquote.controller;

import br.com.devquote.dto.response.PriorityBoardResponse;
import br.com.devquote.service.PriorityBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/priorities")
@RequiredArgsConstructor
@Slf4j
public class PriorityController {

    private final PriorityBoardService priorityBoardService;

    @GetMapping("/board")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriorityBoardResponse> getBoard() {
        return ResponseEntity.ok(priorityBoardService.getBoard());
    }

    @PostMapping("/board/refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PriorityBoardResponse> refresh() {
        priorityBoardService.evict();
        return ResponseEntity.ok(priorityBoardService.getBoard());
    }
}
