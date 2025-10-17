package br.com.devquote.service.impl;

import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;
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
    public void sendTaskCreatedNotification(Task task) {
        log.debug("📧 [EMAIL DISABLED] Would send TASK CREATED notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
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
    public void sendTaskDeletedNotification(Task task) {
        log.debug("📧 [EMAIL DISABLED] Would send TASK DELETED notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
    }

    @Override
    public void sendTaskDeletedNotificationWithAttachments(Task task, List<TaskAttachment> attachments) {
        log.debug("📧 [EMAIL DISABLED] Would send TASK DELETED notification WITH ATTACHMENTS for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
        if (attachments != null && !attachments.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} attachments: {}", 
                    attachments.size(), 
                    attachments.stream().map(TaskAttachment::getOriginalFileName).toList());
        }
    }

    @Override
    public void sendTaskDeletedNotificationWithAttachmentData(Task task, Map<String, byte[]> attachmentDataMap) {
        log.debug("📧 [EMAIL DISABLED] Would send TASK DELETED notification WITH IN-MEMORY ATTACHMENTS for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
        if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} in-memory attachments: {}", 
                    attachmentDataMap.size(), 
                    attachmentDataMap.keySet());
        }
    }

    @Override
    public void sendDeliveryCreatedNotification(Delivery delivery) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY CREATED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
        }
    }

    @Override
    public void sendDeliveryUpdatedNotification(Delivery delivery) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY UPDATED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
        }
    }

    @Override
    public void sendDeliveryDeletedNotification(Delivery delivery) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY DELETED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.debug("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
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
    public void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD notification for: Period ID={}, Month={}, Year={}", 
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear());
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with billing period details");
    }
    
    @Override
    public void sendDeliveryDeletedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap) {
        log.debug("📧 [EMAIL DISABLED] Would send DELIVERY DELETED notification WITH IN-MEMORY ATTACHMENTS for: Delivery ID={}, Status={}",
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
    }

    @Override
    public void sendDeliveryUpdatedNotificationWithAttachmentData(Delivery delivery, Map<String, byte[]> attachmentDataMap) {
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
    }
    
    @Override
    public void sendBillingPeriodNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD notification WITH IN-MEMORY ATTACHMENTS for: Period ID={}, Month={}, Year={}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear());
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with billing period details");
        if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} in-memory attachments: {}",
                    attachmentDataMap.size(),
                    attachmentDataMap.keySet());
        }
    }
    
    @Override
    public void sendBillingPeriodDeletedNotification(BillingPeriod billingPeriod) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD DELETED notification for: Period ID={}, Month={}, Year={}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear());
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with deleted billing period details");
    }
    
    @Override
    public void sendBillingPeriodDeletedNotificationWithAttachmentData(BillingPeriod billingPeriod, Map<String, byte[]> attachmentDataMap) {
        log.debug("📧 [EMAIL DISABLED] Would send BILLING PERIOD DELETED notification WITH IN-MEMORY ATTACHMENTS for: Period ID={}, Month={}, Year={}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear());
        log.debug("📧 [EMAIL DISABLED] Would send to finance department with deleted billing period details");
        if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
            log.debug("📧 [EMAIL DISABLED] Would include {} in-memory attachments: {}",
                    attachmentDataMap.size(),
                    attachmentDataMap.keySet());
        }
    }
}