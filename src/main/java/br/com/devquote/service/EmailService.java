package br.com.devquote.service;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import java.util.List;
import java.util.Map;

public interface EmailService {

    void sendTaskUpdatedNotification(Task task, List<String> additionalEmails);

    void sendDeliveryUpdatedNotification(Delivery delivery, List<String> additionalEmails);

    void sendDeliveryUpdatedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails);

    void sendDeliveryNotificationWhatsApp(Delivery delivery, List<String> additionalWhatsAppRecipients);

    void sendFinancialNotificationAsync(Task task, List<String> additionalEmails);

    void sendFinancialNotificationWhatsApp(Task task, List<String> additionalWhatsAppRecipients);

    void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod, List<String> additionalEmails, String flowType);

    void sendBillingPeriodNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails, String flowType);
}