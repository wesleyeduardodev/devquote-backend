package br.com.devquote.service;

public interface ClickUpSyncService {

    void syncDeliveriesToClickUp();

    boolean syncDeliveryToClickUp(Long deliveryId);
}
