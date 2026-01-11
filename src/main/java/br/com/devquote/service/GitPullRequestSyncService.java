package br.com.devquote.service;

public interface GitPullRequestSyncService {

    void syncMergedPullRequests();

    void syncMergedPullRequestsAsync();

    boolean checkAndUpdatePullRequestStatus(Long deliveryItemId);
}
