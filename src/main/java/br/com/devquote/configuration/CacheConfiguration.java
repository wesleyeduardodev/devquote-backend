package br.com.devquote.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        // Cache do board de prioridades (ClickUp): 10 min para evitar chamadas repetidas;
        // o botão "Atualizar" força refresh (evict), então o usuário controla a atualização.
        cacheManager.registerCustomCache("priorityBoard",
                Caffeine.newBuilder()
                        .maximumSize(50)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
        return cacheManager;
    }
}
