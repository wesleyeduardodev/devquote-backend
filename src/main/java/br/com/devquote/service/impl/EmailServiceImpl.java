package br.com.devquote.service.impl;

import br.com.devquote.configuration.EmailProperties;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;
import br.com.devquote.repository.BillingPeriodTaskRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.service.EmailService;
import br.com.devquote.service.TaskAttachmentService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "devquote.notification.email.enabled", havingValue = "true", matchIfMissing = true)
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties emailProperties;
    private final SubTaskRepository subTaskRepository;
    private final BillingPeriodTaskRepository billingPeriodTaskRepository;
    private final TaskAttachmentService taskAttachmentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @PostConstruct
    public void init() {
        log.debug("EmailService initialized with from address: {}", emailProperties.getFrom());
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendTaskCreatedNotification(Task task) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending task created notification for task ID: {}", task.getId());

            String subject = String.format("DevQuote - Nova tarefa criada: [%s] - %s",
                task.getCode() != null ? task.getCode() : task.getId(),
                task.getTitle() != null ? task.getTitle() : "Sem título");
            String htmlContent = buildTaskCreatedEmailContent(task);

            sendToMultipleRecipients(task, subject, htmlContent, "created");

        } catch (Exception e) {
            log.error("Failed to send task created notification for task ID: {}", task.getId(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendTaskUpdatedNotification(Task task) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending task updated notification for task ID: {}", task.getId());

            String subject = String.format("DevQuote - Tarefa editada: [%s] - %s",
                task.getCode() != null ? task.getCode() : task.getId(),
                task.getTitle() != null ? task.getTitle() : "Sem título");
            String htmlContent = buildTaskUpdatedEmailContent(task);

            sendToMultipleRecipients(task, subject, htmlContent, "updated");

        } catch (Exception e) {
            log.error("Failed to send task updated notification for task ID: {}", task.getId(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendTaskDeletedNotification(Task task) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending task deleted notification for task ID: {}", task.getId());

            String subject = String.format("DevQuote - Tarefa excluída: [%s] - %s",
                task.getCode() != null ? task.getCode() : task.getId(),
                task.getTitle() != null ? task.getTitle() : "Sem título");
            String htmlContent = buildTaskDeletedEmailContent(task);

            sendToMultipleRecipients(task, subject, htmlContent, "deleted");

        } catch (Exception e) {
            log.error("Failed to send task deleted notification for task ID: {}", task.getId(), e);
        }
    }

    private void sendToMultipleRecipients(Task task, String subject, String htmlContent, String action) {
        log.debug("📧 Starting TASK {} email notification process for: Task ID={}, Code={}, Title={}",
                action.toUpperCase(), task.getId(), task.getCode(), task.getTitle());

        String mainRecipient = null;
        String ccRecipient = null;

        // Definir destinatário principal (solicitante)
        if (task.getRequester() != null && task.getRequester().getEmail() != null
            && !task.getRequester().getEmail().trim().isEmpty()) {
            mainRecipient = task.getRequester().getEmail();
            log.debug("📧 Main recipient (requester): {} <{}>",
                    task.getRequester().getName(), task.getRequester().getEmail());
        } else {
            log.warn("📧 ⚠️ Requester email NOT AVAILABLE for task ID: {}. Requester: {}",
                    task.getId(),
                    task.getRequester() != null ? task.getRequester().getName() : "null");
        }

        // Definir destinatário em cópia (você)
        if (emailProperties.getFrom() != null && !emailProperties.getFrom().trim().isEmpty()) {
            // Se solicitante não existe ou tem o mesmo email, você vira destinatário principal
            if (mainRecipient == null || mainRecipient.equals(emailProperties.getFrom())) {
                mainRecipient = emailProperties.getFrom();
                ccRecipient = null;
                log.debug("📧 Sender becomes main recipient: {}", emailProperties.getFrom());
            } else {
                ccRecipient = emailProperties.getFrom();
                log.debug("📧 CC recipient (sender): {}", emailProperties.getFrom());
            }
        } else {
            log.error("📧 ❌ SENDER EMAIL NOT CONFIGURED! Set DEVQUOTE_EMAIL_FROM environment variable");
        }

        // Verificar se há destinatário principal
        if (mainRecipient == null) {
            log.error("📧 ❌ NO VALID RECIPIENTS found for task ID: {} ({} action). Email will NOT be sent!",
                    task.getId(), action);
            return;
        }

        // Carregar anexos da tarefa (entidades para envio por email)
        List<TaskAttachment> taskAttachments = null;
        try {
            taskAttachments = taskAttachmentService.getTaskAttachmentsEntities(task.getId());
            if (taskAttachments != null && !taskAttachments.isEmpty()) {
                log.debug("📎 Found {} attachment(s) for task ID: {}", taskAttachments.size(), task.getId());
            }
        } catch (Exception e) {
            log.warn("📎 Failed to load attachments for task ID: {} - {}", task.getId(), e.getMessage());
            taskAttachments = null; // Continua sem anexos se houver erro
        }

        // Enviar email único com CC e anexos
        try {
            log.debug("📧 Sending TASK {} notification - To: {}, CC: {}, Attachments: {}",
                    action.toUpperCase(), mainRecipient, ccRecipient != null ? ccRecipient : "none",
                    taskAttachments != null ? taskAttachments.size() : 0);

            sendEmailWithAttachments(mainRecipient, ccRecipient, subject, htmlContent, taskAttachments);

            log.debug("📧 ✅ TASK {} notification sent successfully for task ID: {} to: {} (cc: {}, attachments: {})",
                    action.toUpperCase(), task.getId(), mainRecipient, ccRecipient != null ? ccRecipient : "none",
                    taskAttachments != null ? taskAttachments.size() : 0);
        } catch (Exception e) {
            log.error("📧 ❌ FAILED to send {} notification for task ID: {} to: {} (cc: {}) - Error: {}",
                    action, task.getId(), mainRecipient, ccRecipient, e.getMessage(), e);
            throw e;
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
        // Task status removed
        context.setVariable("taskPriority", translatePriority(task.getPriority()));
        context.setVariable("taskType", translateTaskType(task.getTaskType()));
        context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
        context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
        context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
        context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
        context.setVariable("taskNotes", task.getNotes() != null ? task.getNotes() : "");
        context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
        context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));

        // Dados do solicitante
        context.setVariable("requesterName", task.getRequester() != null ? task.getRequester().getName() : "");
        context.setVariable("requesterEmail", task.getRequester() != null && task.getRequester().getEmail() != null ? task.getRequester().getEmail() : "");
        context.setVariable("requesterPhone", task.getRequester() != null && task.getRequester().getPhone() != null ? task.getRequester().getPhone() : "");

        // Buscar subtarefas da tarefa e traduzir status
        java.util.List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

        // Criar lista com dados das subtarefas já traduzidos
        java.util.List<java.util.Map<String, String>> subTasksTranslated = null;
        if (subTasks != null) {
            subTasksTranslated = subTasks.stream().map(subtask -> {
                java.util.Map<String, String> subtaskMap = new java.util.HashMap<>();
                subtaskMap.put("title", subtask.getTitle() != null ? subtask.getTitle() : "");
                subtaskMap.put("description", subtask.getDescription() != null ? subtask.getDescription() : "");
                // Subtask status removed
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

    private String buildTaskUpdatedEmailContent(Task task) {
        Context context = new Context();

        // Dados principais da tarefa
        context.setVariable("task", task);
        context.setVariable("taskId", task.getId());
        context.setVariable("taskCode", task.getCode() != null ? task.getCode() : "");
        context.setVariable("taskTitle", task.getTitle() != null ? task.getTitle() : "");
        context.setVariable("taskDescription", task.getDescription() != null ? task.getDescription() : "");
        // Task status removed
        context.setVariable("taskPriority", translatePriority(task.getPriority()));
        context.setVariable("taskType", translateTaskType(task.getTaskType()));
        context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
        context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
        context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
        context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
        context.setVariable("taskNotes", task.getNotes() != null ? task.getNotes() : "");
        context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
        context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));

        // Dados do solicitante
        context.setVariable("requesterName", task.getRequester() != null ? task.getRequester().getName() : "");
        context.setVariable("requesterEmail", task.getRequester() != null && task.getRequester().getEmail() != null ? task.getRequester().getEmail() : "");
        context.setVariable("requesterPhone", task.getRequester() != null && task.getRequester().getPhone() != null ? task.getRequester().getPhone() : "");

        // Buscar subtarefas da tarefa e traduzir status
        java.util.List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

        // Criar lista com dados das subtarefas já traduzidos
        java.util.List<java.util.Map<String, String>> subTasksTranslated = null;
        if (subTasks != null) {
            subTasksTranslated = subTasks.stream().map(subtask -> {
                java.util.Map<String, String> subtaskMap = new java.util.HashMap<>();
                subtaskMap.put("title", subtask.getTitle() != null ? subtask.getTitle() : "");
                subtaskMap.put("description", subtask.getDescription() != null ? subtask.getDescription() : "");
                // Subtask status removed
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

        return templateEngine.process("email/task-updated", context);
    }

    private String buildTaskDeletedEmailContent(Task task) {
        Context context = new Context();

        // Dados principais da tarefa
        context.setVariable("task", task);
        context.setVariable("taskId", task.getId());
        context.setVariable("taskCode", task.getCode() != null ? task.getCode() : "");
        context.setVariable("taskTitle", task.getTitle() != null ? task.getTitle() : "");
        context.setVariable("taskDescription", task.getDescription() != null ? task.getDescription() : "");
        // Task status removed
        context.setVariable("taskPriority", translatePriority(task.getPriority()));
        context.setVariable("taskType", translateTaskType(task.getTaskType()));
        context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
        context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
        context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
        context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
        context.setVariable("taskNotes", task.getNotes() != null ? task.getNotes() : "");
        context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
        context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));

        // Dados do solicitante
        context.setVariable("requesterName", task.getRequester() != null ? task.getRequester().getName() : "");
        context.setVariable("requesterEmail", task.getRequester() != null && task.getRequester().getEmail() != null ? task.getRequester().getEmail() : "");
        context.setVariable("requesterPhone", task.getRequester() != null && task.getRequester().getPhone() != null ? task.getRequester().getPhone() : "");

        // Buscar subtarefas da tarefa e traduzir status
        java.util.List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

        // Criar lista com dados das subtarefas já traduzidos
        java.util.List<java.util.Map<String, String>> subTasksTranslated = null;
        if (subTasks != null) {
            subTasksTranslated = subTasks.stream().map(subtask -> {
                java.util.Map<String, String> subtaskMap = new java.util.HashMap<>();
                subtaskMap.put("title", subtask.getTitle() != null ? subtask.getTitle() : "");
                subtaskMap.put("description", subtask.getDescription() != null ? subtask.getDescription() : "");
                // Subtask status removed
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

        return templateEngine.process("email/task-deleted", context);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendDeliveryCreatedNotification(Delivery delivery) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending delivery created notification for delivery ID: {}", delivery.getId());

            String subject = String.format("DevQuote - Nova entrega criada: #%d", delivery.getId());
            String htmlContent = buildDeliveryCreatedEmailContent(delivery);

            sendToMultipleRecipientsForDelivery(delivery, subject, htmlContent, "created");

        } catch (Exception e) {
            log.error("Failed to send delivery created notification for delivery ID: {}", delivery.getId(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendDeliveryUpdatedNotification(Delivery delivery) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending delivery updated notification for delivery ID: {}", delivery.getId());

            String subject = String.format("DevQuote - Entrega editada: #%d", delivery.getId());
            String htmlContent = buildDeliveryUpdatedEmailContent(delivery);

            sendToMultipleRecipientsForDelivery(delivery, subject, htmlContent, "updated");

        } catch (Exception e) {
            log.error("Failed to send delivery updated notification for delivery ID: {}", delivery.getId(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendDeliveryDeletedNotification(Delivery delivery) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            log.debug("Sending delivery deleted notification for delivery ID: {}", delivery.getId());

            String subject = String.format("DevQuote - Entrega excluída: #%d", delivery.getId());
            String htmlContent = buildDeliveryDeletedEmailContent(delivery);

            sendToMultipleRecipientsForDelivery(delivery, subject, htmlContent, "deleted");

        } catch (Exception e) {
            log.error("Failed to send delivery deleted notification for delivery ID: {}", delivery.getId(), e);
        }
    }

    private void sendToMultipleRecipientsForDelivery(Delivery delivery, String subject, String htmlContent, String action) {
        String taskInfo = "Unknown";
        if (delivery.getTask() != null) {
            taskInfo = String.format("Task ID=%d, Code=%s",
                    delivery.getTask().getId(),
                    delivery.getTask().getCode());
        }

        log.debug("📧 Starting DELIVERY {} email notification process for: Delivery ID={}, Status={}, {}",
                action.toUpperCase(), delivery.getId(), delivery.getStatus(), taskInfo);

        String mainRecipient = null;
        String ccRecipient = null;

        // Definir destinatário principal (solicitante através da Task)
        if (delivery.getTask() != null
            && delivery.getTask().getRequester() != null
            && delivery.getTask().getRequester().getEmail() != null
            && !delivery.getTask().getRequester().getEmail().trim().isEmpty()) {
            mainRecipient = delivery.getTask().getRequester().getEmail();
            String requesterName = delivery.getTask().getRequester().getName();
            log.debug("📧 Main recipient (requester): {} <{}>",
                    requesterName, mainRecipient);
        } else {
            log.warn("📧 ⚠️ Requester email NOT AVAILABLE for delivery ID: {}. Task chain: {}",
                    delivery.getId(),
                    delivery.getTask() != null ?
                        (delivery.getTask().getRequester() != null ? "Requester has no email" : "No requester")
                        : "No task");
        }

        // Definir destinatário em cópia (você)
        if (emailProperties.getFrom() != null && !emailProperties.getFrom().trim().isEmpty()) {
            // Se solicitante não existe ou tem o mesmo email, você vira destinatário principal
            if (mainRecipient == null || mainRecipient.equals(emailProperties.getFrom())) {
                mainRecipient = emailProperties.getFrom();
                ccRecipient = null;
                log.debug("📧 Sender becomes main recipient: {}", emailProperties.getFrom());
            } else {
                ccRecipient = emailProperties.getFrom();
                log.debug("📧 CC recipient (sender): {}", emailProperties.getFrom());
            }
        } else {
            log.error("📧 ❌ SENDER EMAIL NOT CONFIGURED! Set DEVQUOTE_EMAIL_FROM environment variable");
        }

        // Verificar se há destinatário principal
        if (mainRecipient == null) {
            log.error("📧 ❌ NO VALID RECIPIENTS found for delivery ID: {} ({} action). Email will NOT be sent!",
                    delivery.getId(), action);
            return;
        }

        // Enviar email único com CC
        try {
            log.debug("📧 Sending DELIVERY {} notification - To: {}, CC: {}",
                    action.toUpperCase(), mainRecipient, ccRecipient != null ? ccRecipient : "none");

            sendEmailWithCC(mainRecipient, ccRecipient, subject, htmlContent);

            log.debug("📧 ✅ DELIVERY {} notification sent successfully for delivery ID: {} to: {} (cc: {})",
                    action.toUpperCase(), delivery.getId(), mainRecipient, ccRecipient != null ? ccRecipient : "none");
        } catch (Exception e) {
            log.error("📧 ❌ FAILED to send delivery {} notification for delivery ID: {} to: {} (cc: {}) - Error: {}",
                    action, delivery.getId(), mainRecipient, ccRecipient, e.getMessage(), e);
            throw e;
        }
    }

    private String buildDeliveryCreatedEmailContent(Delivery delivery) {
        Context context = new Context();

        // Reutilizar a mesma lógica de construção de contexto
        buildDeliveryEmailContext(context, delivery);

        return templateEngine.process("email/delivery-created", context);
    }

    private String buildDeliveryUpdatedEmailContent(Delivery delivery) {
        Context context = new Context();

        // Reutilizar a mesma lógica de construção de contexto
        buildDeliveryEmailContext(context, delivery);

        return templateEngine.process("email/delivery-updated", context);
    }

    private String buildDeliveryDeletedEmailContent(Delivery delivery) {
        Context context = new Context();

        // Reutilizar a mesma lógica de construção de contexto
        buildDeliveryEmailContext(context, delivery);

        return templateEngine.process("email/delivery-deleted", context);
    }

    private void buildDeliveryEmailContext(Context context, Delivery delivery) {
        // Dados principais da entrega
        context.setVariable("delivery", delivery);
        context.setVariable("deliveryId", delivery.getId());
        context.setVariable("deliveryStatus", translateDeliveryStatus(delivery.getStatus()));
        context.setVariable("createdAt", delivery.getCreatedAt().format(DATE_FORMATTER));

        // Dados dos itens da entrega (nova arquitetura)
        if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
            // Remover dados técnicos da seção principal - eles ficam apenas nos itens
            context.setVariable("deliveryBranch", "");
            context.setVariable("deliverySourceBranch", "");
            context.setVariable("deliveryPullRequest", "");
            context.setVariable("deliveryScript", "");
            context.setVariable("deliveryNotes", "");
            context.setVariable("deliveryStartedAt", "");
            context.setVariable("deliveryFinishedAt", "");
            
            // Lista completa de itens para templates - aqui ficam todos os dados técnicos
            var translatedItems = delivery.getItems().stream()
                .map(item -> {
                    var map = new java.util.HashMap<String, Object>();
                    map.put("project", item.getProject());
                    map.put("status", translateDeliveryStatus(item.getStatus()));
                    map.put("branch", item.getBranch());
                    map.put("sourceBranch", item.getSourceBranch());
                    map.put("pullRequest", item.getPullRequest());
                    map.put("script", item.getScript());
                    map.put("notes", item.getNotes());
                    map.put("startedAt", item.getStartedAt());
                    map.put("finishedAt", item.getFinishedAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            context.setVariable("deliveryItems", translatedItems);
            context.setVariable("hasMultipleItems", delivery.getItems().size() >= 1); // Sempre mostrar itens quando houver
        } else {
            // Valores padrão se não houver itens
            context.setVariable("deliveryBranch", "");
            context.setVariable("deliverySourceBranch", "");
            context.setVariable("deliveryPullRequest", "");
            context.setVariable("deliveryScript", "");
            context.setVariable("deliveryNotes", "");
            context.setVariable("deliveryStartedAt", "");
            context.setVariable("deliveryFinishedAt", "");
            context.setVariable("deliveryItems", java.util.Collections.emptyList());
            context.setVariable("hasMultipleItems", false);
        }

        // Dados do orçamento/tarefa
        if (delivery.getTask() != null) {
            context.setVariable("quoteCode", delivery.getTask().getCode() != null ? delivery.getTask().getCode() : "");
            context.setVariable("quoteName", delivery.getTask().getTitle() != null ? delivery.getTask().getTitle() : "");
            context.setVariable("requesterName", delivery.getTask().getRequester() != null ? delivery.getTask().getRequester().getName() : "");
            context.setVariable("requesterEmail", delivery.getTask().getRequester() != null && delivery.getTask().getRequester().getEmail() != null ? delivery.getTask().getRequester().getEmail() : "");
        } else {
            context.setVariable("quoteCode", "");
            context.setVariable("quoteName", "");
            context.setVariable("requesterName", "");
            context.setVariable("requesterEmail", "");
        }
    }

    private String translateDeliveryStatus(br.com.devquote.enums.DeliveryStatus status) {
        if (status == null) return "N/A";
        return switch (status) {
            case PENDING -> "Pendente";
            case DEVELOPMENT -> "Desenvolvimento";
            case DELIVERED -> "Entregue";
            case HOMOLOGATION -> "Homologação";
            case APPROVED -> "Aprovado";
            case REJECTED -> "Rejeitado";
            case PRODUCTION -> "Produção";
        };
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        sendEmailWithCC(to, null, subject, htmlContent);
    }

    private void sendEmailWithCC(String to, String cc, String subject, String htmlContent) {
        sendEmailWithAttachments(to, cc, subject, htmlContent, null);
    }

    private void sendEmailWithAttachments(String to, String cc, String subject, String htmlContent, List<TaskAttachment> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);

            if (cc != null && !cc.trim().isEmpty()) {
                helper.setCc(cc);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Adicionar anexos se disponíveis
            if (attachments != null && !attachments.isEmpty()) {
                log.debug("Adding {} attachments to email", attachments.size());
                for (TaskAttachment attachment : attachments) {
                    try {
                        // Criar InputStreamSource que gera stream fresco para cada chamada
                        org.springframework.core.io.InputStreamSource inputStreamSource = new org.springframework.core.io.InputStreamSource() {
                            @Override
                            public java.io.InputStream getInputStream() throws java.io.IOException {
                                org.springframework.core.io.Resource resource = taskAttachmentService.downloadAttachment(attachment.getId());
                                return resource.getInputStream();
                            }
                        };
                        
                        helper.addAttachment(attachment.getOriginalFileName(), inputStreamSource);
                        log.debug("Attached file: {} ({})", attachment.getOriginalFileName(), attachment.getContentType());
                    } catch (Exception e) {
                        log.warn("Failed to attach file: {} - {}", attachment.getOriginalFileName(), e.getMessage());
                        // Continua o processamento mesmo se um anexo falhar
                    }
                }
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} (cc: {})", to, cc, e);
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
            case "DEVELOPMENT" -> "in-progress";
            case "DELIVERED" -> "delivered";
            case "HOMOLOGATION" -> "testing";
            case "PRODUCTION" -> "production";
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

    @Override
    @Async("emailTaskExecutor")
    public void sendFinancialNotificationAsync(Task task) {
        if (!emailProperties.isEnabled()) {
            log.warn("Email notifications are disabled. Skipping financial notification for task ID: {}", task.getId());
            return;
        }

        String financeEmail = emailProperties.getFinanceEmail();
        if (financeEmail == null || financeEmail.trim().isEmpty()) {
            log.error("Finance email not configured. Cannot send financial notification for task ID: {}", task.getId());
            return;
        }

        try {
            log.debug("Sending financial notification for task ID: {} to {}", task.getId(), financeEmail);

            Context context = new Context();
            context.setVariable("task", task);
            // Task status removed
            context.setVariable("priorityTranslation", translatePriority(task.getPriority()));

            // Buscar subtarefas via repository se necessário
            if (task.getHasSubTasks()) {
                List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

                // Criar lista com dados das subtarefas já traduzidos
                List<java.util.Map<String, Object>> subTasksTranslated = subTasks.stream().map(subtask -> {
                    java.util.Map<String, Object> subtaskMap = new java.util.HashMap<>();
                    subtaskMap.put("title", subtask.getTitle() != null ? subtask.getTitle() : "");
                    subtaskMap.put("description", subtask.getDescription() != null ? subtask.getDescription() : "");
                    // Subtask status removed
                    subtaskMap.put("amount", subtask.getAmount());
                    return subtaskMap;
                }).collect(java.util.stream.Collectors.toList());

                context.setVariable("subTasks", subTasksTranslated);
            }

            // Usar valor total da tarefa (já calculado) ou 0 se nulo
            java.math.BigDecimal totalAmount = task.getAmount() != null ? task.getAmount() : java.math.BigDecimal.ZERO;
            context.setVariable("totalAmount", totalAmount);

            String htmlContent = templateEngine.process("email/financial-notification", context);

            // Se o email financeiro for diferente do email remetente, colocar remetente em CC
            String ccRecipient = null;
            if (!financeEmail.equals(emailProperties.getFrom())) {
                ccRecipient = emailProperties.getFrom();
            }

            log.debug("📧 Sending FINANCIAL notification - To: {}, CC: {}",
                    financeEmail, ccRecipient != null ? ccRecipient : "none");

            sendEmailWithCC(financeEmail, ccRecipient, "💰 Notificação Financeira - Tarefa " + task.getCode(), htmlContent);

            log.debug("Financial notification sent successfully for task ID: {} to {}", task.getId(), financeEmail);

        } catch (Exception e) {
            log.error("Unexpected error while sending financial notification for task ID: {} to {}: {}",
                task.getId(), financeEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send financial notification email", e);
        }
    }

    @Override
    @Async
    public void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriod) {
        if (!emailProperties.isEnabled()) {
            log.warn("Email notifications are disabled. Skipping billing period notification for period ID: {}", billingPeriod.getId());
            return;
        }

        String financeEmail = emailProperties.getFinanceEmail();
        if (financeEmail == null || financeEmail.trim().isEmpty()) {
            log.error("Finance email not configured. Cannot send billing period notification for period ID: {}", billingPeriod.getId());
            return;
        }

        try {
            log.debug("Sending billing period notification for period ID: {} ({}/{}) to {}",
                billingPeriod.getId(), billingPeriod.getMonth(), billingPeriod.getYear(), financeEmail);

            Context context = new Context();
            context.setVariable("billingPeriod", billingPeriod);

            // Array com nomes dos meses em português
            String[] monthNames = {"janeiro", "fevereiro", "março", "abril", "maio", "junho",
                                   "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};
            String monthName = monthNames[billingPeriod.getMonth() - 1];

            // Formato: "julho/2025"
            context.setVariable("monthYear", String.format("%s/%d", monthName, billingPeriod.getYear()));

            // Formato: "Medição julho de 2025"
            context.setVariable("measurementPeriod", String.format("Medição %s de %d", monthName, billingPeriod.getYear()));

            // Formatar data de pagamento
            if (billingPeriod.getPaymentDate() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                context.setVariable("paymentDate", billingPeriod.getPaymentDate().format(formatter));
            } else {
                context.setVariable("paymentDate", null);
            }

            // Buscar tarefas vinculadas ao período de faturamento
            List<Object[]> billingTasks = billingPeriodTaskRepository.findTasksWithDetailsByBillingPeriodId(billingPeriod.getId());

            // Processar tarefas e calcular totais
            List<java.util.Map<String, Object>> tasksData = new java.util.ArrayList<>();
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

            for (Object[] taskData : billingTasks) {
                java.util.Map<String, Object> taskMap = new java.util.HashMap<>();
                taskMap.put("code", taskData[1]); // task_code
                taskMap.put("title", taskData[2]); // task_title
                taskMap.put("description", taskData[3]); // task_description
                taskMap.put("amount", taskData[4]); // task_amount
                tasksData.add(taskMap);

                if (taskData[4] != null) {
                    totalAmount = totalAmount.add((java.math.BigDecimal) taskData[4]);
                }
            }

            context.setVariable("tasks", tasksData);
            context.setVariable("totalAmount", totalAmount);
            context.setVariable("taskCount", billingTasks.size());

            String htmlContent = templateEngine.process("email/billing-period-notification", context);

            // Se o email financeiro for diferente do email remetente, colocar remetente em CC
            String ccRecipient = null;
            if (!financeEmail.equals(emailProperties.getFrom())) {
                ccRecipient = emailProperties.getFrom();
            }

            String subject = "📊 Faturamento Mensal - " + String.format("%02d/%d", billingPeriod.getMonth(), billingPeriod.getYear());

            log.debug("📧 Sending BILLING PERIOD notification - To: {}, CC: {}",
                    financeEmail, ccRecipient != null ? ccRecipient : "none");

            sendEmailWithCC(financeEmail, ccRecipient, subject, htmlContent);

            log.debug("Billing period notification sent successfully for period ID: {} to {}", billingPeriod.getId(), financeEmail);

        } catch (Exception e) {
            log.error("Unexpected error while sending billing period notification for period ID: {} to {}: {}",
                billingPeriod.getId(), financeEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send billing period notification email", e);
        }
    }
}
