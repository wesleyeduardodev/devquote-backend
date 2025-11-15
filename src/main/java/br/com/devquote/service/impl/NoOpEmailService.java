package br.com.devquote.service.impl;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.service.EmailService;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(name = "devquote.notification.email.enabled", havingValue = "false")
public class NoOpEmailService implements EmailService {

    @PostConstruct
    public void init() {
        log.debug("========================================");
        log.debug("NoOpEmailService ACTIVATED - Email notifications are DISABLED");
        log.debug("All email notifications will be skipped and logged only");
        log.debug("To enable emails, set DEVQUOTE_EMAIL_ENABLED=true");
        log.debug("========================================");
    }

    @Override
    public void sendTaskUpdatedNotification(Task task, List<String> additionalEmails) {
        log.debug("📧 [EMAIL DISABLED] Would send TASK UPDATED notification for: Task ID={}, Code={}, Title={}",
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>",
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

    @Override
    public void sendDeliveryUpdatedNotification(Delivery delivery, List<String> additionalEmails) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY UPDATED notification for: Delivery ID={}, Status={}",
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null &&
                delivery.getTask().getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>",
                    delivery.getTask().getRequester().getName(),
                    delivery.getTask().getRequester().getEmail());
        }
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

    @Override
    public void sendDeliveryNotificationWhatsApp(Delivery delivery, List<String> additionalWhatsAppRecipients) {
        log.debug("📱 [WHATSAPP] Would send DELIVERY WhatsApp notification for: Delivery ID={}, Status={}",
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && delivery.getTask().getRequester() != null) {
            log.debug("📱 [WHATSAPP] Would send to requester phone: {}",
                    delivery.getTask().getRequester().getPhone());
        }
        if (additionalWhatsAppRecipients != null && !additionalWhatsAppRecipients.isEmpty()) {
            log.debug("📱 [WHATSAPP] Would include {} additional WhatsApp recipient(s): {}",
                    additionalWhatsAppRecipients.size(), additionalWhatsAppRecipients);
        }
    }

    @Override
    public void sendFinancialNotificationAsync(Task task, List<String> additionalEmails) {
        log.debug("📧 [EMAIL DISABLED] Would send FINANCIAL notification for: Task ID={}, Code={}, Title={}",
                task.getId(), task.getCode(), task.getTitle());
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with task amount: {}",
                task.getAmount() != null ? task.getAmount() : "0.00");
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

    @Override
    public void sendFinancialNotificationWhatsApp(Task task, List<String> additionalWhatsAppRecipients) {
        log.debug("📱 [WHATSAPP] Would send FINANCIAL WhatsApp notification for: Task ID={}, Code={}, Title={}",
                task.getId(), task.getCode(), task.getTitle());
        log.debug("📱 [WHATSAPP] Would send to finance department with task amount: {}",
                task.getAmount() != null ? task.getAmount() : "0.00");
        if (additionalWhatsAppRecipients != null && !additionalWhatsAppRecipients.isEmpty()) {
            log.debug("📱 [WHATSAPP] Would include {} additional WhatsApp recipient(s): {}",
                    additionalWhatsAppRecipients.size(), additionalWhatsAppRecipients);
        }
    }

    @Override
    public void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod, List<String> additionalEmails, String flowType) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD notification for: Period ID={}, Month={}, Year={}, FlowType={}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear(), flowType);
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with billing period details");
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

    @Override
    public void sendDeliveryUpdatedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY UPDATED notification WITH IN-MEMORY ATTACHMENTS for: Delivery ID={}, Status={}",
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && delivery.getTask().getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>",
                    delivery.getTask().getRequester().getName(),
                    delivery.getTask().getRequester().getEmail());
        }
        if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} in-memory attachments: {}",
                    attachmentDataMap.size(),
                    attachmentDataMap.keySet());
        }
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

    @Override
    public void sendBillingPeriodNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails, String flowType) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD notification WITH IN-MEMORY ATTACHMENTS for: Period ID={}, Month={}, Year={}, FlowType={}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear(), flowType);
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with billing period details");
        if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} in-memory attachments: {}",
                    attachmentDataMap.size(),
                    attachmentDataMap.keySet());
        }
        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} additional email(s) in CC: {}",
                    additionalEmails.size(), additionalEmails);
        }
    }

}