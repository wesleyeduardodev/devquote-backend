package br.com.devquote.service;

import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;

import java.util.List;
import java.util.Map;

public interface EmailService {
    
    void sendTaskCreatedNotification(Task task);

    void sendTaskUpdatedNotification(Task task, List<String> additionalEmails);

    void sendTaskDeletedNotification(Task task);
    
    void sendTaskDeletedNotificationWithAttachments(Task task, List<TaskAttachment> attachments);
    
    void sendTaskDeletedNotificationWithAttachmentData(Task task, Map<String, byte[]> attachmentDataMap);
    
    void sendDeliveryCreatedNotification(Delivery delivery);
    
    void sendDeliveryUpdatedNotification(Delivery delivery, List<String> additionalEmails);

    void sendDeliveryDeletedNotification(Delivery delivery);

    void sendDeliveryDeletedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap);

    void sendDeliveryUpdatedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails);

    void sendFinancialNotificationAsync(Task task, List<String> additionalEmails);

    void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod);
    
    void sendBillingPeriodNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap);
    
    void sendBillingPeriodDeletedNotification(BillingPeriod billingPeriod);
    
    void sendBillingPeriodDeletedNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap);
    
}