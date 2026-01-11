package br.com.devquote.job;

import br.com.devquote.service.ClickUpSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickUpSyncJob {

    private final ClickUpSyncService clickUpSyncService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void executeDailySync() {
        log.info("=== INICIO DO JOB: Sincronizacao de Entregas com ClickUp ===");

        long startTime = System.currentTimeMillis();

        try {
            clickUpSyncService.syncDeliveriesToClickUp();
        } catch (Exception e) {
            log.error("Erro durante execucao do job de sincronizacao ClickUp: {}", e.getMessage(), e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== FIM DO JOB: Sincronizacao de Entregas com ClickUp ({}ms) ===", duration);
    }
}
