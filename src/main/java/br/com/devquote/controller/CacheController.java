package br.com.devquote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Cache", description = "Gerenciamento de cache - Somente ADMIN")
public class CacheController {

    private final CacheManager cacheManager;

    @GetMapping
    @Operation(summary = "Listar todos os caches disponiveis no sistema")
    public ResponseEntity<Map<String, Object>> getAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCaches", cacheNames.size());
        response.put("cacheNames", cacheNames);
        response.put("cacheType", "Caffeine (in-memory)");
        log.info("Total de {} caches configurados no sistema", cacheNames.size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cacheName}")
    @Operation(summary = "Limpar todas as chaves de um cache especifico")
    public ResponseEntity<Map<String, Object>> evictCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", cacheName,
                    "status", "Cache nao encontrado"
            ));
        }
        cache.clear();
        log.info("Cache {} - Todas as chaves foram removidas", cacheName);
        return ResponseEntity.ok(Map.of(
                "cacheName", cacheName,
                "status", "Cache limpo com sucesso"
        ));
    }

    @DeleteMapping
    @Operation(summary = "Limpar todos os caches do sistema")
    public ResponseEntity<Map<String, Object>> evictAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("Todos os {} caches foram limpos", cacheNames.size());
        return ResponseEntity.ok(Map.of(
                "totalCachesCleared", cacheNames.size(),
                "status", "Todos os caches foram limpos com sucesso"
        ));
    }
}
