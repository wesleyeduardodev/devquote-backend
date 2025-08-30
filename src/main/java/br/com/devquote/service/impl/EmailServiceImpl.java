package br.com.devquote.service.impl;

import br.com.devquote.configuration.EmailProperties;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "devquote.notification.email.enabled", havingValue = "true", matchIfMissing = true)
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;
    private final SubTaskRepository subTaskRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Async("emailTaskExecutor")
    public void sendTaskCreatedNotification(Task task) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.info("Sending task created notification for task ID: {}", task.getId());
            
            String subject = String.format("DevQuote - Nova tarefa criada: [%s] - %s", 
                task.getCode() != null ? task.getCode() : task.getId(), 
                task.getTitle() != null ? task.getTitle() : "Sem título");
            String htmlContent = buildTaskCreatedEmailContent(task);
            
            sendEmail(emailProperties.getTo(), subject, htmlContent);
            
            log.info("Task created notification sent successfully for task ID: {}", task.getId());
            
        } catch (Exception e) {
            log.error("Failed to send task created notification for task ID: {}", task.getId(), e);
        }
    }

    private String buildTaskCreatedEmailContent(Task task) {
        Context context = new Context();
        
        // Dados principais da tarefa
        context.setVariable("task", task);
        context.setVariable("taskId", task.getId());
        context.setVariable("taskCode", task.getCode() != null ? task.getCode() : "");
        context.setVariable("taskTitle", task.getTitle() != null ? task.getTitle() : "");
        context.setVariable("taskDescription", task.getDescription() != null ? task.getDescription() : "");
        context.setVariable("taskStatus", translateStatus(task.getStatus()));
        context.setVariable("taskPriority", translatePriority(task.getPriority()));
        context.setVariable("taskType", translateTaskType(task.getTaskType()));
        context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
        context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
        context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
        context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
        context.setVariable("taskNotes", task.getNotes() != null ? task.getNotes() : "");
        context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
        context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));
        
        // Buscar subtarefas da tarefa e traduzir status
        java.util.List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());
        
        // Criar lista com dados das subtarefas já traduzidos
        java.util.List<java.util.Map<String, String>> subTasksTranslated = null;
        if (subTasks != null) {
            subTasksTranslated = subTasks.stream().map(subtask -> {
                java.util.Map<String, String> subtaskMap = new java.util.HashMap<>();
                subtaskMap.put("title", subtask.getTitle() != null ? subtask.getTitle() : "");
                subtaskMap.put("description", subtask.getDescription() != null ? subtask.getDescription() : "");
                subtaskMap.put("status", translateStatus(subtask.getStatus()));
                return subtaskMap;
            }).collect(java.util.stream.Collectors.toList());
        }
        
        context.setVariable("hasSubTasks", subTasks != null && !subTasks.isEmpty());
        context.setVariable("subTasks", subTasksTranslated);
        
        // Função para traduzir status e obter classes CSS (disponível no template)
        context.setVariable("translateStatus", new Object() {
            public String translate(String status) {
                return translateStatus(status);
            }
        });
        context.setVariable("getStatusCssClass", new Object() {
            public String getCssClass(String status) {
                return getStatusCssClass(status);
            }
        });
        context.setVariable("getPriorityCssClass", new Object() {
            public String getCssClass(String priority) {
                return getPriorityCssClass(priority);
            }
        });
        
        return templateEngine.process("email/task-created", context);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "N/A";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pendente";
            case "IN_PROGRESS" -> "Em Progresso";
            case "COMPLETED" -> "Concluída";
            case "CANCELLED" -> "Cancelada";
            case "ON_HOLD" -> "Em Espera";
            case "BLOCKED" -> "Bloqueada";
            case "REVIEWING" -> "Em Revisão";
            default -> status;
        };
    }

    private String translatePriority(String priority) {
        if (priority == null) return "";
        return switch (priority.toUpperCase()) {
            case "LOW" -> "Baixa";
            case "MEDIUM" -> "Média";
            case "HIGH" -> "Alta";
            case "URGENT" -> "Urgente";
            case "CRITICAL" -> "Crítica";
            case "VERY_LOW" -> "Muito Baixa";
            case "VERY_HIGH" -> "Muito Alta";
            case "IMMEDIATE" -> "Imediata";
            case "NORMAL" -> "Normal";
            default -> priority;
        };
    }

    private String translateTaskType(String taskType) {
        if (taskType == null) return "";
        return switch (taskType.toUpperCase()) {
            case "BUG" -> "Bug/Correção";
            case "ENHANCEMENT" -> "Melhoria";
            case "NEW_FEATURE" -> "Nova Funcionalidade";
            case "FEATURE" -> "Funcionalidade";
            case "MAINTENANCE" -> "Manutenção";
            case "DOCUMENTATION" -> "Documentação";
            case "REFACTOR" -> "Refatoração";
            case "TEST" -> "Teste";
            case "RESEARCH" -> "Pesquisa";
            case "SUPPORT" -> "Suporte";
            default -> taskType;
        };
    }

    private String getStatusCssClass(String status) {
        if (status == null) return "pending";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "pending";
            case "IN_PROGRESS" -> "in-progress";
            case "COMPLETED" -> "completed";
            case "CANCELLED" -> "cancelled";
            case "ON_HOLD" -> "on-hold";
            case "BLOCKED" -> "blocked";
            case "REVIEWING" -> "reviewing";
            case "APPROVED" -> "approved";
            case "REJECTED" -> "rejected";
            case "DRAFT" -> "draft";
            case "ACTIVE" -> "active";
            case "INACTIVE" -> "inactive";
            case "PAUSED" -> "paused";
            case "REOPENED" -> "reopened";
            default -> "pending";
        };
    }

    private String getPriorityCssClass(String priority) {
        if (priority == null) return "media";
        return switch (priority.toUpperCase()) {
            case "LOW" -> "baixa";
            case "MEDIUM" -> "media";
            case "HIGH", "URGENT", "CRITICAL" -> "alta";
            case "VERY_LOW" -> "baixa";
            case "VERY_HIGH", "IMMEDIATE" -> "alta";
            case "NORMAL" -> "media";
            default -> "media";
        };
    }
}