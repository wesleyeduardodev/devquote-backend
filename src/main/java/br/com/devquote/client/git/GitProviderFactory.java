package br.com.devquote.client.git;

import br.com.devquote.error.GitProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitProviderFactory {

    private final List<GitProviderClient> providers;

    public GitProviderClient getProvider(String pullRequestUrl) {
        return findProvider(pullRequestUrl)
                .orElseThrow(() -> new GitProviderException(
                        "Provedor nao suportado para URL: " + pullRequestUrl,
                        "UNSUPPORTED_PROVIDER"
                ));
    }

    public Optional<GitProviderClient> findProvider(String pullRequestUrl) {
        if (pullRequestUrl == null || pullRequestUrl.trim().isEmpty()) {
            return Optional.empty();
        }

        return providers.stream()
                .filter(provider -> provider.supports(pullRequestUrl))
                .findFirst();
    }

    public boolean isSupported(String pullRequestUrl) {
        return findProvider(pullRequestUrl).isPresent();
    }
}
