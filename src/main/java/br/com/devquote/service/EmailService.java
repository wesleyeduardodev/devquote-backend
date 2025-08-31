package br.com.devquote.service;

import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;

public interface EmailService {
    
    void sendTaskCreatedNotification(Task task);
    
    void sendTaskUpdatedNotification(Task task);
    
    void sendTaskDeletedNotification(Task task);
    
    void sendDeliveryCreatedNotification(Delivery delivery);
    
    void sendDeliveryUpdatedNotification(Delivery delivery);
    
    void sendDeliveryDeletedNotification(Delivery delivery);
    
    void sendFinancialNotificationAsync(Task task);
    
}