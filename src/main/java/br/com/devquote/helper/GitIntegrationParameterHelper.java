package br.com.devquote.helper;

import br.com.devquote.configuration.IntegrationsProperties;
import br.com.devquote.error.GitProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitIntegrationParameterHelper {

    private final IntegrationsProperties integrationsProperties;

    public boolean isIntegrationEnabled() {
        return Boolean.TRUE.equals(integrationsProperties.getGithub().getEnabled());
    }

    public String getGitHubToken() {
        String token = integrationsProperties.getGithub().getToken();
        if (token == null || token.trim().isEmpty()) {
            throw new GitProviderException("Token do GitHub nao configurado", "GITHUB_TOKEN_NOT_CONFIGURED");
        }
        return token;
    }
}
