package br.com.devquote.service.impl;

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
        log.debug("Email notifications are disabled - skipping notification for task ID: {}", task.getId());
    }
}