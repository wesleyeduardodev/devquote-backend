package br.com.devquote.service.impl;

import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "devquote.notification.email.enabled", havingValue = "false")
public class NoOpEmailService implements EmailService {

    @Override
    public void sendTaskCreatedNotification(Task task) {
        log.debug("Email notifications are disabled - skipping task created notification for task ID: {}", task.getId());
    }

    @Override
    public void sendTaskUpdatedNotification(Task task) {
        log.debug("Email notifications are disabled - skipping task updated notification for task ID: {}", task.getId());
    }

    @Override
    public void sendTaskDeletedNotification(Task task) {
        log.debug("Email notifications are disabled - skipping task deleted notification for task ID: {}", task.getId());
    }

    @Override
    public void sendDeliveryCreatedNotification(Delivery delivery) {
        log.debug("Email notifications are disabled - skipping delivery created notification for delivery ID: {}", delivery.getId());
    }

    @Override
    public void sendDeliveryUpdatedNotification(Delivery delivery) {
        log.debug("Email notifications are disabled - skipping delivery updated notification for delivery ID: {}", delivery.getId());
    }

    @Override
    public void sendDeliveryDeletedNotification(Delivery delivery) {
        log.debug("Email notifications are disabled - skipping delivery deleted notification for delivery ID: {}", delivery.getId());
    }
}