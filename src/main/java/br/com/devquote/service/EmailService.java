package br.com.devquote.service;

import br.com.devquote.entity.Task;

public interface EmailService {
    
    void sendTaskCreatedNotification(Task task);
    
    void sendTaskUpdatedNotification(Task task);
    
    void sendTaskDeletedNotification(Task task);
    
}