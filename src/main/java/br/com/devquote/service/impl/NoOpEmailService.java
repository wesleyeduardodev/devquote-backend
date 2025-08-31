package br.com.devquote.service.impl;

import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.service.EmailService;
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
        log.warn("========================================");
        log.warn("NoOpEmailService ACTIVATED - Email notifications are DISABLED");
        log.warn("All email notifications will be skipped and logged only");
        log.warn("To enable emails, set DEVQUOTE_EMAIL_ENABLED=true");
        log.warn("========================================");
    }

    @Override
    public void sendTaskCreatedNotification(Task task) {
        log.warn("📧 [EMAIL DISABLED] Would send TASK CREATED notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
    }

    @Override
    public void sendTaskUpdatedNotification(Task task) {
        log.warn("📧 [EMAIL DISABLED] Would send TASK UPDATED notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
    }

    @Override
    public void sendTaskDeletedNotification(Task task) {
        log.warn("📧 [EMAIL DISABLED] Would send TASK DELETED notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        if (task.getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    task.getRequester().getName(), task.getRequester().getEmail());
        }
    }

    @Override
    public void sendDeliveryCreatedNotification(Delivery delivery) {
        log.warn("📧 [EMAIL DISABLED] Would send DELIVERY CREATED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
        }
    }

    @Override
    public void sendDeliveryUpdatedNotification(Delivery delivery) {
        log.warn("📧 [EMAIL DISABLED] Would send DELIVERY UPDATED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
        }
    }

    @Override
    public void sendDeliveryDeletedNotification(Delivery delivery) {
        log.warn("📧 [EMAIL DISABLED] Would send DELIVERY DELETED notification for: Delivery ID={}, Status={}", 
                delivery.getId(), delivery.getStatus());
        if (delivery.getTask() != null && 
            delivery.getTask().getRequester() != null) {
            log.warn("📧 [EMAIL DISABLED] Would send to requester: {} <{}>", 
                    delivery.getTask().getRequester().getName(), 
                    delivery.getTask().getRequester().getEmail());
        }
    }

    @Override
    public void sendFinancialNotificationAsync(Task task) {
        log.warn("📧 [EMAIL DISABLED] Would send FINANCIAL notification for: Task ID={}, Code={}, Title={}", 
                task.getId(), task.getCode(), task.getTitle());
        log.warn("📧 [EMAIL DISABLED] Would send to finance department with task amount: {}", 
                task.getAmount() != null ? task.getAmount() : "0.00");
    }
}