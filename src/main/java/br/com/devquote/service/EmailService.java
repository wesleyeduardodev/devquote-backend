package br.com.devquote.service;

import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;

import java.util.List;

public interface EmailService {
    
    void sendTaskCreatedNotification(Task task);
    
    void sendTaskUpdatedNotification(Task task);
    
    void sendTaskDeletedNotification(Task task);
    
    void sendTaskDeletedNotificationWithAttachments(Task task, List<TaskAttachment> attachments);
    
    void sendDeliveryCreatedNotification(Delivery delivery);
    
    void sendDeliveryUpdatedNotification(Delivery delivery);
    
    void sendDeliveryDeletedNotification(Delivery delivery);
    
    void sendFinancialNotificationAsync(Task task);
    
    void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod);
    
}