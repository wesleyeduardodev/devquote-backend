package br.com.devquote.service;

import br.com.devquote.dto.response.SyncPullRequestsResponse;

public interface ClickUpSyncService {

    void syncDeliveriesToClickUp();

    boolean syncDeliveryToClickUp(Long deliveryId);

    /**
     * Propaga os PRs dos items de uma delivery específica pro ClickUp (campo Branch + descrição).
     * Manual, disparado pela UI. Implementa no-op detection (não escreve se nada mudou).
     */
    SyncPullRequestsResponse syncPullRequestsForDelivery(Long deliveryId);
}
