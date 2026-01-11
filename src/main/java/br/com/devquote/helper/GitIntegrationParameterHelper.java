package br.com.devquote.helper;

import br.com.devquote.error.GitProviderException;
import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitIntegrationParameterHelper {

    private static final String GIT_INTEGRATION_ENABLED = "GIT_INTEGRATION_ENABLED";
    private static final String GITHUB_TOKEN = "GITHUB_TOKEN";

    private final SystemParameterService systemParameterService;

    public boolean isIntegrationEnabled() {
        return systemParameterService.getBoolean(GIT_INTEGRATION_ENABLED, false);
    }

    public String getGitHubToken() {
        try {
            String token = systemParameterService.getString(GITHUB_TOKEN);
            if (token == null || token.trim().isEmpty()) {
                throw new GitProviderException("Token do GitHub nao configurado", "GITHUB_TOKEN_NOT_CONFIGURED");
            }
            return token;
        } catch (GitProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter token do GitHub: {}", e.getMessage());
            throw new GitProviderException("Token do GitHub nao encontrado: " + e.getMessage(), "GITHUB_TOKEN_NOT_FOUND");
        }
    }
}
