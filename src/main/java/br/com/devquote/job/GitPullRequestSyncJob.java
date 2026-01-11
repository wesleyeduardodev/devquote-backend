package br.com.devquote.job;

import br.com.devquote.service.GitPullRequestSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitPullRequestSyncJob {

    private final GitPullRequestSyncService gitPullRequestSyncService;

    @Scheduled(cron = "0 0 6 * * ?")
    public void executeDailySync() {
        log.info("=== INICIO DO JOB: Sincronizacao de PRs Mergeados ===");

        long startTime = System.currentTimeMillis();

        try {
            gitPullRequestSyncService.syncMergedPullRequests();
        } catch (Exception e) {
            log.error("Erro durante execucao do job de sincronizacao: {}", e.getMessage(), e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== FIM DO JOB: Sincronizacao de PRs Mergeados ({}ms) ===", duration);
    }
}
