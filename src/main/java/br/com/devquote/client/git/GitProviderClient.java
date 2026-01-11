package br.com.devquote.client.git;

public interface GitProviderClient {

    boolean checkIfMerged(String pullRequestUrl);

    boolean supports(String pullRequestUrl);

    String getProviderName();
}
