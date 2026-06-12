package br.com.devquote.job;

import br.com.devquote.service.GitPullRequestSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job diario que analisa os itens de entrega do fluxo DESENVOLVIMENTO com PR
 * preenchido: se o PR ja foi mergeado no GitHub, marca o item como PRODUCTION e
 * recalcula o status agregado da entrega.
 *
 * Escopo: somente local (git/PR), sem empurrar pro ClickUp.
 * O proprio service curto-circuita se a integracao com Git estiver desabilitada
 * (GITHUB_INTEGRATION_ENABLED=false), entao no dev local o job e no-op.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitPullRequestSyncJob {

    private final GitPullRequestSyncService gitPullRequestSyncService;

    @Scheduled(cron = "${jobs.git-pr-sync.cron}", zone = "America/Sao_Paulo")
    public void run() {
        log.info("[JOB] GitPullRequestSyncJob disparado");
        gitPullRequestSyncService.syncMergedPullRequestsDevelopmentFlow();
    }
}
