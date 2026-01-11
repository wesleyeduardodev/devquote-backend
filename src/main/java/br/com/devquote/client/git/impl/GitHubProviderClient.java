package br.com.devquote.client.git.impl;

import br.com.devquote.client.git.GitProviderClient;
import br.com.devquote.dto.response.GitHubPullRequestResponse;
import br.com.devquote.error.GitProviderException;
import br.com.devquote.helper.GitIntegrationParameterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubProviderClient implements GitProviderClient {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final Pattern GITHUB_PR_PATTERN = Pattern.compile(
            "https?://github\\.com/([^/]+)/([^/]+)/pull/(\\d+)"
    );

    private final RestTemplate gitHubRestTemplate;
    private final GitIntegrationParameterHelper parameterHelper;

    @Override
    public boolean supports(String pullRequestUrl) {
        return pullRequestUrl != null && GITHUB_PR_PATTERN.matcher(pullRequestUrl.trim()).matches();
    }

    @Override
    public boolean checkIfMerged(String pullRequestUrl) {
        Matcher matcher = GITHUB_PR_PATTERN.matcher(pullRequestUrl.trim());
        if (!matcher.find()) {
            throw new GitProviderException("URL de PR invalida: " + pullRequestUrl, "INVALID_PR_URL");
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        int prNumber = Integer.parseInt(matcher.group(3));

        log.debug("Consultando PR: {}/{} #{}", owner, repo, prNumber);

        String url = String.format("%s/repos/%s/%s/pulls/%d", GITHUB_API_BASE, owner, repo, prNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + parameterHelper.getGitHubToken());
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubPullRequestResponse> response = gitHubRestTemplate.exchange(
                    url, HttpMethod.GET, entity, GitHubPullRequestResponse.class
            );

            GitHubPullRequestResponse prResponse = response.getBody();
            if (prResponse == null) {
                log.warn("Resposta vazia da API GitHub para PR: {}", pullRequestUrl);
                return false;
            }

            log.debug("PR {}/{} #{} - merged: {}", owner, repo, prNumber, prResponse.getMerged());
            return Boolean.TRUE.equals(prResponse.getMerged());

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("PR nao encontrado: {}", pullRequestUrl);
            throw new GitProviderException("PR nao encontrado: " + pullRequestUrl, "GITHUB_PR_NOT_FOUND");

        } catch (HttpClientErrorException.Forbidden e) {
            String rateLimitRemaining = e.getResponseHeaders() != null
                    ? e.getResponseHeaders().getFirst("X-RateLimit-Remaining")
                    : null;
            if ("0".equals(rateLimitRemaining)) {
                log.error("Rate limit excedido na API do GitHub");
                throw new GitProviderException("Rate limit excedido", "GITHUB_RATE_LIMIT_EXCEEDED");
            }
            log.error("Acesso negado ao GitHub: {}", e.getMessage());
            throw new GitProviderException("Acesso negado: " + e.getMessage(), "GITHUB_FORBIDDEN");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP ao consultar GitHub - Status: {} - Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new GitProviderException("Erro ao consultar GitHub: " + e.getStatusCode(), "GITHUB_HTTP_ERROR");

        } catch (Exception e) {
            log.error("Erro inesperado ao consultar GitHub: {}", e.getMessage(), e);
            throw new GitProviderException("Erro inesperado: " + e.getMessage(), "GITHUB_UNEXPECTED_ERROR");
        }
    }

    @Override
    public String getProviderName() {
        return "GitHub";
    }
}
