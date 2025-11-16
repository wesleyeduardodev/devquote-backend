package br.com.devquote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Cache", description = "Gerenciamento de cache Redis - Somente ADMIN")
public class CacheController {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/system-parameters")
    @Operation(summary = "Listar todas as chaves do cache systemParameters com seus valores")
    public ResponseEntity<Map<String, Object>> getAllKeys() {
        Cache cache = cacheManager.getCache("systemParameters");
        if (cache == null) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", "systemParameters",
                    "status", "Cache não encontrado",
                    "entries", Collections.emptyList()
            ));
        }

        List<Map<String, Object>> entries = new ArrayList<>();
        if (cache instanceof RedisCache) {
            Set<String> nativeKeys = redisTemplate.keys("systemParameters::*");
            if (nativeKeys != null) {
                for (String key : nativeKeys) {
                    String cleanKey = key.replace("systemParameters::", "");
                    Cache.ValueWrapper valueWrapper = cache.get(cleanKey);

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("cacheKey", cleanKey);
                    entry.put("value", valueWrapper != null ? valueWrapper.get() : null);
                    entries.add(entry);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("cacheName", "systemParameters");
        response.put("totalEntries", entries.size());
        response.put("entries", entries);
        response.put("ttl", "1800000ms (30 minutos)");

        log.info("Cache systemParameters - Total de {} entradas encontradas", entries.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-parameters/{key}")
    @Operation(summary = "Ver valor de uma chave específica do cache (por name do parâmetro)")
    public ResponseEntity<Map<String, Object>> getKeyValue(@PathVariable String key) {
        Cache cache = cacheManager.getCache("systemParameters");
        if (cache == null) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", "systemParameters",
                    "key", key,
                    "status", "Cache não encontrado"
            ));
        }

        String cacheKey = "name:" + key;
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        Map<String, Object> response = new HashMap<>();
        response.put("cacheName", "systemParameters");
        response.put("parameterName", key);
        response.put("cacheKey", cacheKey);

        if (valueWrapper != null) {
            response.put("exists", true);
            response.put("value", valueWrapper.get());
            log.info("Cache hit - Key: {}", cacheKey);
        } else {
            response.put("exists", false);
            response.put("value", null);
            log.info("Cache miss - Key: {}", cacheKey);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/system-parameters/{key}")
    @Operation(summary = "Limpar uma chave específica do cache (por name do parâmetro)")
    public ResponseEntity<Map<String, Object>> evictKey(@PathVariable String key) {
        Cache cache = cacheManager.getCache("systemParameters");
        if (cache == null) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", "systemParameters",
                    "parameterName", key,
                    "status", "Cache não encontrado"
            ));
        }

        String cacheKey = "name:" + key;
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper == null) {
            log.warn("Tentativa de remover chave inexistente - Key: {}", cacheKey);
            return ResponseEntity.ok(Map.of(
                    "cacheName", "systemParameters",
                    "parameterName", key,
                    "cacheKey", cacheKey,
                    "exists", false,
                    "status", "Chave não existe no cache"
            ));
        }

        cache.evict(cacheKey);
        log.info("Cache evicted - Key: {}", cacheKey);

        return ResponseEntity.ok(Map.of(
                "cacheName", "systemParameters",
                "parameterName", key,
                "cacheKey", cacheKey,
                "exists", true,
                "status", "Chave removida do cache com sucesso"
        ));
    }

    @DeleteMapping("/system-parameters")
    @Operation(summary = "Limpar todas as chaves do cache systemParameters")
    public ResponseEntity<Map<String, Object>> evictAll() {
        Cache cache = cacheManager.getCache("systemParameters");
        if (cache == null) {
            return ResponseEntity.ok(Map.of(
                    "cacheName", "systemParameters",
                    "status", "Cache não encontrado"
            ));
        }

        cache.clear();

        log.info("Cache systemParameters - Todas as chaves foram removidas");

        return ResponseEntity.ok(Map.of(
                "cacheName", "systemParameters",
                "status", "Todas as chaves foram removidas do cache com sucesso"
        ));
    }

    @GetMapping
    @Operation(summary = "Listar todos os caches disponíveis no sistema")
    public ResponseEntity<Map<String, Object>> getAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();

        Map<String, Object> response = new HashMap<>();
        response.put("totalCaches", cacheNames.size());
        response.put("cacheNames", cacheNames);

        log.info("Total de {} caches configurados no sistema", cacheNames.size());

        return ResponseEntity.ok(response);
    }
}
