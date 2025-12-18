package br.com.devquote.service.impl;
import br.com.devquote.configuration.EmailProperties;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.NotificationConfig;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.TaskAttachment;
import br.com.devquote.enums.Environment;
import br.com.devquote.enums.FlowType;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import br.com.devquote.repository.BillingPeriodTaskRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.service.EmailService;
import br.com.devquote.service.NotificationConfigService;
import br.com.devquote.service.TaskAttachmentService;
import br.com.devquote.service.WhatsAppService;
import br.com.devquote.service.storage.FileStorageStrategy;
import br.com.devquote.utils.HtmlImageExtractor;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final FileStorageStrategy fileStorageStrategy;
    private final NotificationConfigService notificationConfigService;
    private final WhatsAppService whatsAppService;
    private final br.com.devquote.repository.TaskRepository taskRepository;
    private final br.com.devquote.repository.DeliveryRepository deliveryRepository;
    private final br.com.devquote.repository.BillingPeriodRepository billingPeriodRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @PostConstruct
    public void init() {
        log.debug("EmailService initialized with from address: {}", emailProperties.getFrom());
    }

    @Override
    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendTaskUpdatedNotification(Task taskParam, List<String> additionalEmails) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Task task = taskRepository.findById(taskParam.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found: " + taskParam.getId()));

            log.debug("Sending task updated notification for task ID: {}", task.getId());

            String subject = String.format("DevQuote - Dados da Tarefa: [%s] - %s",
                    task.getCode() != null ? task.getCode() : task.getId(),
                    task.getTitle() != null ? task.getTitle() : "Sem título");
            String htmlContent = buildTaskUpdatedEmailContent(task);

            sendTaskDataEmailWithNotificationConfig(task, subject, htmlContent, additionalEmails);

        } catch (Exception e) {
            log.error("Failed to send task updated notification for task ID: {}", taskParam.getId(), e);
        }
    }

    private NotificationConfig findNotificationConfig(NotificationConfigType configType, NotificationType notificationType) {
        try {
            return notificationConfigService.findEntityByConfigTypeAndNotificationType(configType, notificationType);
        } catch (Exception e) {
            log.warn("Erro ao buscar configuração de notificação para {} - {}: {}", configType, notificationType, e.getMessage());
            return null;
        }
    }


    private void sendTaskDataEmailWithNotificationConfig(Task task, String subject, String htmlContent, List<String> additionalEmails) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_DADOS_TAREFA, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_DADOS_TAREFA + EMAIL. Task ID: {}", task.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            if (task.getRequester() != null && task.getRequester().getEmail() != null
                    && !task.getRequester().getEmail().trim().isEmpty()) {
                toEmails.add(task.getRequester().getEmail());
            }
        } else {
            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for task data notification. Task ID: {}",
                    additionalEmails.size(), task.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for task data notification. Task ID: {}, Config ID: {}",
                    task.getId(), config.getId());
            return;
        }

        List<TaskAttachment> taskAttachments;
        try {
            log.info("📎 LOADING attachments for TASK DATA notification - task ID: {}", task.getId());
            taskAttachments = taskAttachmentService.getTaskAttachmentsEntities(task.getId());
            if (taskAttachments != null && !taskAttachments.isEmpty()) {
                log.info("📎 ✅ Found {} attachment(s) for TASK DATA notification - task ID: {} - Files: {}",
                        taskAttachments.size(), task.getId(),
                        taskAttachments.stream().map(TaskAttachment::getOriginalFileName).toList());
            } else {
                log.info("📎 No attachments found for TASK DATA notification - task ID: {}", task.getId());
            }
        } catch (Exception e) {
            log.error("📎 ❌ FAILED to load attachments for TASK DATA notification - task ID: {} - Error: {}",
                    task.getId(), e.getMessage(), e);
            taskAttachments = null;
        }

        log.debug("📧 Sending TASK DATA notification with config - To: {}, CC: {}, Attachments: {}",
                toEmails, ccEmails.isEmpty() ? "none" : ccEmails,
                taskAttachments != null ? taskAttachments.size() : 0);

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithAttachments(toEmail, ccRecipientsString, subject, htmlContent, taskAttachments);
                log.debug("Task data notification sent successfully for task ID: {} to {}", task.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send task data notification for task ID: {} to {}: {}",
                        task.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    private void sendEmailWithInMemoryAttachments(String to, String cc, String subject, String htmlContent, Map<String, byte[]> attachmentDataMap) {
        log.info("📧 SENDWITHINDEMEMORYATTACHMENTS called - To: {}, CC: {}, Subject: {}, Attachments: {}",
                to, cc != null ? cc : "none", subject, attachmentDataMap != null ? attachmentDataMap.size() : 0);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("📧 Setting email headers - From: {}, To: {}, Subject: {}", emailProperties.getFrom(), to, subject);

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);

            if (cc != null && !cc.trim().isEmpty()) {
                if (cc.contains(",")) {
                    String[] ccArray = cc.split(",");
                    for (int i = 0; i < ccArray.length; i++) {
                        ccArray[i] = ccArray[i].trim();
                    }
                    helper.setCc(ccArray);
                } else {
                    helper.setCc(cc.trim());
                }
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (attachmentDataMap != null && !attachmentDataMap.isEmpty()) {
                log.debug("📎 Adding {} IN-MEMORY attachments to email", attachmentDataMap.size());
                for (Map.Entry<String, byte[]> entry : attachmentDataMap.entrySet()) {
                    try {
                        String fileName = entry.getKey();
                        byte[] fileData = entry.getValue();

                        org.springframework.core.io.InputStreamSource inputStreamSource = new org.springframework.core.io.InputStreamSource() {
                            @Override
                            public java.io.InputStream getInputStream() throws java.io.IOException {
                                return new java.io.ByteArrayInputStream(fileData);
                            }
                        };

                        helper.addAttachment(fileName, inputStreamSource);
                        log.debug("📎 ✅ Successfully attached IN-MEMORY file: {} ({} bytes)", fileName, fileData.length);

                    } catch (Exception e) {
                        log.warn("📎 ❌ Failed to attach IN-MEMORY file: {} - Error: {}", entry.getKey(), e.getMessage());
                    }
                }
            }

            log.info("📧 CALLING mailSender.send() - Final step to send email with IN-MEMORY attachments to: {}", to);
            mailSender.send(message);
            log.info("📧 ✅ EMAIL WITH IN-MEMORY ATTACHMENTS SENT successfully via mailSender to: {}", to);

        } catch (MessagingException e) {
            log.error("📧 ❌ MESSAGING EXCEPTION - Failed to send email with IN-MEMORY attachments to: {} (cc: {}) - Error: {}", to, cc, e.getMessage(), e);
            throw new RuntimeException("Failed to send email with in-memory attachments", e);
        } catch (Exception e) {
            log.error("📧 ❌ GENERAL EXCEPTION - Failed to send email with IN-MEMORY attachments to: {} (cc: {}) - Error: {}", to, cc, e.getMessage(), e);
            throw new RuntimeException("Failed to send email with in-memory attachments", e);
        }
    }

    private String buildTaskUpdatedEmailContent(Task task) {
        Context context = new Context();

        context.setVariable("task", task);
        context.setVariable("taskId", task.getId());
        context.setVariable("taskCode", task.getCode() != null ? task.getCode() : "");
        context.setVariable("taskTitle", convertLineBreaksToHtml(task.getTitle()));
        context.setVariable("taskDescription", prepareHtmlForEmail(task.getDescription()));

        context.setVariable("taskPriority", translatePriority(task.getPriority()));
        context.setVariable("taskType", translateTaskType(task.getTaskType()));
        context.setVariable("taskEnvironment", translateEnvironment(task.getEnvironment()));
        context.setVariable("taskFlowType", task.getFlowType() != null ? task.getFlowType().name() : "");
        context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
        context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
        context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
        context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
        context.setVariable("taskNotes", "");
        context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
        context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));

        context.setVariable("requesterName", task.getRequester() != null ? task.getRequester().getName() : "");
        context.setVariable("requesterEmail", task.getRequester() != null && task.getRequester().getEmail() != null ? task.getRequester().getEmail() : "");
        context.setVariable("requesterPhone", task.getRequester() != null && task.getRequester().getPhone() != null ? task.getRequester().getPhone() : "");

        List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

        List<Map<String, String>> subTasksTranslated = null;
        if (subTasks != null) {
            subTasksTranslated = subTasks.stream().map(subtask -> {
                Map<String, String> subtaskMap = new HashMap<>();
                subtaskMap.put("title", convertLineBreaksToHtml(subtask.getTitle()));
                subtaskMap.put("description", prepareHtmlForEmail(subtask.getDescription()));
                return subtaskMap;
            }).collect(Collectors.toList());
        }

        context.setVariable("hasSubTasks", subTasks != null && !subTasks.isEmpty());
        context.setVariable("subTasks", subTasksTranslated);

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

    @Override
    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendDeliveryUpdatedNotification(Delivery deliveryParam, List<String> additionalEmails) {
        if (!emailProperties.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Delivery delivery = deliveryRepository.findById(deliveryParam.getId())
                    .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryParam.getId()));

            Map<String, byte[]> allInlineImages = new java.util.LinkedHashMap<>();
            int[] extractCounter = {1};
            int[] replaceCounter = {1};

            if (delivery.getTask() != null && delivery.getTask().getDescription() != null) {
                Map<String, byte[]> taskDescImages = extractInlineImagesWithCounter(delivery.getTask().getDescription(), extractCounter);
                allInlineImages.putAll(taskDescImages);
            }

            if (delivery.getNotes() != null) {
                Map<String, byte[]> deliveryNotesImages = extractInlineImagesWithCounter(delivery.getNotes(), extractCounter);
                allInlineImages.putAll(deliveryNotesImages);
            }

            if (delivery.getItems() != null) {
                for (var item : delivery.getItems()) {
                    if (item.getNotes() != null) {
                        Map<String, byte[]> itemNotesImages = extractInlineImagesWithCounter(item.getNotes(), extractCounter);
                        allInlineImages.putAll(itemNotesImages);
                    }
                }
            }

            if (delivery.getOperationalItems() != null) {
                for (var item : delivery.getOperationalItems()) {
                    if (item.getDescription() != null) {
                        Map<String, byte[]> itemDescImages = extractInlineImagesWithCounter(item.getDescription(), extractCounter);
                        allInlineImages.putAll(itemDescImages);
                    }
                }
            }

            String subject = String.format("DevQuote - Dados da Entrega: #%d", delivery.getId());
            String htmlContent = buildDeliveryUpdatedEmailContent(delivery, replaceCounter);

            if (!allInlineImages.isEmpty()) {
                log.info("📎 Extracted {} inline image(s) from delivery fields for notification - delivery ID: {} - Files: {}",
                        allInlineImages.size(), delivery.getId(), allInlineImages.keySet());
            }

            sendDeliveryEmailWithNotificationConfig(delivery, subject, htmlContent, additionalEmails, allInlineImages);

        } catch (Exception e) {
            log.error("Failed to send delivery updated notification for delivery ID: {}", deliveryParam.getId(), e);
        }
    }

    private void sendDeliveryEmailWithNotificationConfig(Delivery delivery, String subject, String htmlContent, List<String> additionalEmails, Map<String, byte[]> inlineImages) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_ENTREGA, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_ENTREGA + EMAIL. Delivery ID: {}", delivery.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {

            if (delivery.getTask() != null && delivery.getTask().getRequester() != null
                    && delivery.getTask().getRequester().getEmail() != null
                    && !delivery.getTask().getRequester().getEmail().trim().isEmpty()) {
                toEmails.add(delivery.getTask().getRequester().getEmail());
            }
        } else {

            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {

            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for delivery notification. Delivery ID: {}",
                    additionalEmails.size(), delivery.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for delivery notification. Delivery ID: {}, Config ID: {}",
                    delivery.getId(), config.getId());
            return;
        }

        log.debug("📧 Sending DELIVERY notification with config - To: {}, CC: {}, InlineImages: {}",
                toEmails, ccEmails.isEmpty() ? "none" : ccEmails,
                inlineImages != null ? inlineImages.size() : 0);

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithAttachments(toEmail, ccRecipientsString, subject, htmlContent, null, inlineImages);
                log.debug("Delivery notification sent successfully for delivery ID: {} to {}", delivery.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send delivery notification for delivery ID: {} to {}: {}",
                        delivery.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    private String buildDeliveryUpdatedEmailContent(Delivery delivery) {
        return buildDeliveryUpdatedEmailContent(delivery, new int[]{1});
    }

    private String buildDeliveryUpdatedEmailContent(Delivery delivery, int[] replaceCounter) {
        Context context = new Context();

        buildDeliveryEmailContext(context, delivery, replaceCounter);

        return templateEngine.process("email/delivery-updated", context);
    }

    private void buildDeliveryEmailContext(Context context, Delivery delivery) {
        buildDeliveryEmailContext(context, delivery, new int[]{1});
    }

    private void buildDeliveryEmailContext(Context context, Delivery delivery, int[] replaceCounter) {

        context.setVariable("delivery", delivery);
        context.setVariable("deliveryId", delivery.getId());
        context.setVariable("deliveryStatus", translateDeliveryStatus(delivery.getStatus()));
        context.setVariable("createdAt", delivery.getCreatedAt().format(DATE_FORMATTER));

        if (delivery.getTask() != null) {
            context.setVariable("quoteCode", delivery.getTask().getCode() != null ? delivery.getTask().getCode() : "");
            context.setVariable("quoteName", delivery.getTask().getTitle() != null ? delivery.getTask().getTitle() : "");
            context.setVariable("quoteDescription", prepareHtmlForEmailWithCounter(delivery.getTask().getDescription(), replaceCounter));
            context.setVariable("taskFlowType", translateFlowType(delivery.getTask().getFlowType()));
            context.setVariable("taskType", translateTaskType(delivery.getTask().getTaskType()));
            context.setVariable("taskEnvironment", translateEnvironment(delivery.getTask().getEnvironment()));
            context.setVariable("taskAmount", delivery.getTask().getAmount() != null ? delivery.getTask().getAmount() : java.math.BigDecimal.ZERO);
            context.setVariable("taskAmountFormatted", formatCurrency(delivery.getTask().getAmount()));
            context.setVariable("requesterName", delivery.getTask().getRequester() != null ? delivery.getTask().getRequester().getName() : "");
            context.setVariable("requesterEmail", delivery.getTask().getRequester() != null && delivery.getTask().getRequester().getEmail() != null ? delivery.getTask().getRequester().getEmail() : "");
        } else {
            context.setVariable("quoteCode", "");
            context.setVariable("quoteName", "");
            context.setVariable("quoteDescription", "");
            context.setVariable("taskFlowType", "");
            context.setVariable("taskType", "");
            context.setVariable("taskEnvironment", "");
            context.setVariable("taskAmount", java.math.BigDecimal.ZERO);
            context.setVariable("taskAmountFormatted", "R$ 0,00");
            context.setVariable("requesterName", "");
            context.setVariable("requesterEmail", "");
        }

        context.setVariable("notes", delivery.getNotes() != null ? prepareHtmlForEmailWithCounter(delivery.getNotes(), replaceCounter) : "");

        var allTranslatedItems = new java.util.ArrayList<java.util.HashMap<String, Object>>();

        if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
            for (var item : delivery.getItems()) {
                var map = new java.util.HashMap<String, Object>();
                map.put("project", item.getProject());
                map.put("status", translateDeliveryStatus(item.getStatus()));
                map.put("branch", item.getBranch());
                map.put("sourceBranch", item.getSourceBranch());
                map.put("pullRequest", item.getPullRequest());
                map.put("notes", prepareHtmlForEmailWithCounter(item.getNotes(), replaceCounter));
                map.put("startedAt", item.getStartedAt());
                map.put("finishedAt", item.getFinishedAt());
                allTranslatedItems.add(map);
            }
        }

        if (delivery.getOperationalItems() != null && !delivery.getOperationalItems().isEmpty()) {
            for (var item : delivery.getOperationalItems()) {
                var map = new java.util.HashMap<String, Object>();
                map.put("title", item.getTitle());
                map.put("description", prepareHtmlForEmailWithCounter(item.getDescription(), replaceCounter));
                map.put("status", translateOperationalItemStatus(item.getStatus()));
                map.put("startedAt", item.getStartedAt());
                map.put("finishedAt", item.getFinishedAt());
                allTranslatedItems.add(map);
            }
        }

        if (!allTranslatedItems.isEmpty()) {
            context.setVariable("deliveryBranch", "");
            context.setVariable("deliverySourceBranch", "");
            context.setVariable("deliveryPullRequest", "");
            context.setVariable("deliveryScript", "");
            context.setVariable("deliveryNotes", "");
            context.setVariable("deliveryStartedAt", "");
            context.setVariable("deliveryFinishedAt", "");
            context.setVariable("deliveryItems", allTranslatedItems);
            context.setVariable("hasMultipleItems", allTranslatedItems.size() >= 1);
        } else {

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
            case CANCELLED -> "Cancelado";
        };
    }

    private String translateFlowType(br.com.devquote.enums.FlowType flowType) {
        if (flowType == null) return "";
        return switch (flowType) {
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case OPERACIONAL -> "Operacional";
        };
    }

    private String translateOperationalItemStatus(br.com.devquote.enums.OperationalItemStatus status) {
        if (status == null) return "N/A";
        return switch (status) {
            case PENDING -> "Pendente";
            case DELIVERED -> "Entregue";
            case CANCELLED -> "Cancelado";
        };
    }

    private String translateTaskType(String taskType) {
        if (taskType == null || taskType.isEmpty()) return "";
        return switch (taskType.toUpperCase()) {

            case "BACKUP" -> "Backup";
            case "DEPLOY" -> "Deploy";
            case "LOGS" -> "Logs";
            case "DATABASE_APPLICATION" -> "Aplicação de Banco";
            case "NEW_SERVER" -> "Novo Servidor";
            case "MONITORING" -> "Monitoramento";
            case "SUPPORT" -> "Suporte";

            case "BUG" -> "Bug";
            case "ENHANCEMENT" -> "Melhoria";
            case "NEW_FEATURE" -> "Nova Funcionalidade";
            default -> taskType;
        };
    }

    private String translateEnvironment(br.com.devquote.enums.Environment environment) {
        if (environment == null) return "";
        return switch (environment) {
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case HOMOLOGACAO -> "Homologação";
            case PRODUCAO -> "Produção";
        };
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return currencyFormatter.format(amount);
    }

    private void sendEmailWithAttachments(String to, String cc, String subject, String htmlContent, List<TaskAttachment> attachments) {
        sendEmailWithAttachments(to, cc, subject, htmlContent, attachments, null);
    }

    private void sendEmailWithAttachments(String to, String cc, String subject, String htmlContent, List<TaskAttachment> attachments, Map<String, byte[]> inlineImages) {
        int totalAttachments = (attachments != null ? attachments.size() : 0) + (inlineImages != null ? inlineImages.size() : 0);
        log.info("📧 SENDWITHATTACHMENTS called - To: {}, CC: {}, Subject: {}, Attachments: {}, InlineImages: {}",
                to, cc != null ? cc : "none", subject, attachments != null ? attachments.size() : 0, inlineImages != null ? inlineImages.size() : 0);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("📧 Setting email headers - From: {}, To: {}, Subject: {}", emailProperties.getFrom(), to, subject);

            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);

            if (cc != null && !cc.trim().isEmpty()) {

                if (cc.contains(",")) {
                    String[] ccArray = cc.split(",");

                    for (int i = 0; i < ccArray.length; i++) {
                        ccArray[i] = ccArray[i].trim();
                    }
                    helper.setCc(ccArray);
                } else {
                    helper.setCc(cc.trim());
                }
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (inlineImages != null && !inlineImages.isEmpty()) {
                log.debug("Adding {} inline images as attachments", inlineImages.size());
                for (Map.Entry<String, byte[]> entry : inlineImages.entrySet()) {
                    try {
                        String imageName = entry.getKey();
                        byte[] imageBytes = entry.getValue();
                        org.springframework.core.io.InputStreamSource inputStreamSource = () -> new java.io.ByteArrayInputStream(imageBytes);
                        helper.addAttachment(imageName, inputStreamSource);
                        log.debug("Attached inline image: {} ({} bytes)", imageName, imageBytes.length);
                    } catch (Exception e) {
                        log.warn("Failed to attach inline image: {} - {}", entry.getKey(), e.getMessage());
                    }
                }
            }

            if (attachments != null && !attachments.isEmpty()) {
                log.debug("Adding {} attachments to email", attachments.size());
                for (TaskAttachment attachment : attachments) {
                    try {

                        org.springframework.core.io.InputStreamSource inputStreamSource = () -> fileStorageStrategy.getFileStream(attachment.getFilePath());

                        helper.addAttachment(attachment.getOriginalFileName(), inputStreamSource);
                        log.debug("Attached file: {} ({})", attachment.getOriginalFileName(), attachment.getContentType());
                    } catch (Exception e) {
                        log.warn("Failed to attach file: {} - {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }

            log.info("📧 CALLING mailSender.send() - Final step to send email to: {}", to);
            mailSender.send(message);
            log.info("📧 ✅ EMAIL SENT successfully via mailSender to: {}", to);

        } catch (MessagingException e) {
            log.error("📧 ❌ MESSAGING EXCEPTION - Failed to send email to: {} (cc: {}) - Error: {}", to, cc, e.getMessage(), e);
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
    @Transactional(readOnly = true)
    public void sendFinancialNotificationAsync(Task taskParam, List<String> additionalEmails) {
        if (!emailProperties.isEnabled()) {
            log.warn("Email notifications are disabled. Skipping financial notification for task ID: {}", taskParam.getId());
            return;
        }

        try {
            Task task = taskRepository.findById(taskParam.getId())
                    .orElseThrow(() -> new RuntimeException("Task not found: " + taskParam.getId()));

            Map<String, byte[]> allInlineImages = new java.util.LinkedHashMap<>();
            int[] extractCounter = {1};
            int[] replaceCounter = {1};

            Map<String, byte[]> taskImages = extractInlineImagesWithCounter(task.getDescription(), extractCounter);
            allInlineImages.putAll(taskImages);

            List<SubTask> subTasks = task.getHasSubTasks() ? subTaskRepository.findByTaskId(task.getId()) : new ArrayList<>();
            for (SubTask subtask : subTasks) {
                Map<String, byte[]> subtaskImages = extractInlineImagesWithCounter(subtask.getDescription(), extractCounter);
                allInlineImages.putAll(subtaskImages);
            }

            Context context = new Context();

            context.setVariable("task", task);
            context.setVariable("taskId", task.getId());
            context.setVariable("taskCode", task.getCode() != null ? task.getCode() : "");
            context.setVariable("taskTitle", convertLineBreaksToHtml(task.getTitle()));
            context.setVariable("taskDescription", prepareHtmlForEmailWithCounter(task.getDescription(), replaceCounter));
            context.setVariable("taskPriority", translatePriority(task.getPriority()));
            context.setVariable("taskType", translateTaskType(task.getTaskType()));
            context.setVariable("taskEnvironment", translateEnvironment(task.getEnvironment()));
            context.setVariable("taskFlowType", task.getFlowType() != null ? task.getFlowType().name() : "");
            context.setVariable("taskSystemModule", task.getSystemModule() != null ? task.getSystemModule() : "");
            context.setVariable("taskServerOrigin", task.getServerOrigin() != null ? task.getServerOrigin() : "");
            context.setVariable("taskLink", task.getLink() != null ? task.getLink() : "");
            context.setVariable("taskMeetingLink", task.getMeetingLink() != null ? task.getMeetingLink() : "");
            context.setVariable("taskNotes", "");
            context.setVariable("createdBy", task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema");
            context.setVariable("createdAt", task.getCreatedAt().format(DATE_FORMATTER));

            context.setVariable("requesterName", task.getRequester() != null ? task.getRequester().getName() : "");
            context.setVariable("requesterEmail", task.getRequester() != null && task.getRequester().getEmail() != null ? task.getRequester().getEmail() : "");
            context.setVariable("requesterPhone", task.getRequester() != null && task.getRequester().getPhone() != null ? task.getRequester().getPhone() : "");

            context.setVariable("priorityTranslation", translatePriority(task.getPriority()));

            if (task.getHasSubTasks() && !subTasks.isEmpty()) {
                List<java.util.Map<String, Object>> subTasksTranslated = subTasks.stream().map(subtask -> {
                    java.util.Map<String, Object> subtaskMap = new java.util.HashMap<>();
                    subtaskMap.put("title", convertLineBreaksToHtml(subtask.getTitle()));
                    subtaskMap.put("description", prepareHtmlForEmailWithCounter(subtask.getDescription(), replaceCounter));
                    subtaskMap.put("amount", subtask.getAmount());
                    return subtaskMap;
                }).collect(java.util.stream.Collectors.toList());

                context.setVariable("subTasks", subTasksTranslated);
            }

            BigDecimal totalAmount = task.getAmount() != null ? task.getAmount() : BigDecimal.ZERO;
            context.setVariable("totalAmount", totalAmount);

            String htmlContent = templateEngine.process("email/financial-notification", context);
            String subject = "💰 Notificação Financeira - Tarefa " + task.getCode();

            sendFinancialEmailWithNotificationConfig(task, subject, htmlContent, additionalEmails, allInlineImages);

        } catch (Exception e) {
            log.error("Unexpected error while sending financial notification for task ID: {}: {}",
                    taskParam.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send financial notification email", e);
        }
    }

    @Override
    public void sendFinancialNotificationWhatsApp(Task task, List<String> additionalWhatsAppRecipients) {
        try {
            sendFinancialWhatsAppWithNotificationConfig(task, additionalWhatsAppRecipients);
        } catch (Exception e) {
            log.error("Unexpected error while sending financial WhatsApp notification for task ID: {}: {}",
                    task.getId(), e.getMessage(), e);
        }
    }

    private void sendFinancialEmailWithNotificationConfig(Task task, String subject, String htmlContent, List<String> additionalEmails, Map<String, byte[]> inlineImages) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_ORCAMENTO_TAREFA, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_ORCAMENTO_TAREFA + EMAIL. Task ID: {}", task.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            if (task.getRequester() != null && task.getRequester().getEmail() != null
                    && !task.getRequester().getEmail().trim().isEmpty()) {
                toEmails.add(task.getRequester().getEmail());
            }
        } else {
            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {
            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for task ID: {}",
                    additionalEmails.size(), task.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for financial notification. Task ID: {}, Config ID: {}",
                    task.getId(), config.getId());
            return;
        }

        List<TaskAttachment> taskAttachments;
        try {
            log.info("📎 LOADING attachments for FINANCIAL notification - task ID: {}", task.getId());
            taskAttachments = taskAttachmentService.getTaskAttachmentsEntities(task.getId());
            if (taskAttachments != null && !taskAttachments.isEmpty()) {
                log.info("📎 ✅ Found {} attachment(s) for FINANCIAL notification - task ID: {} - Files: {}",
                        taskAttachments.size(), task.getId(),
                        taskAttachments.stream().map(TaskAttachment::getOriginalFileName).toList());
            } else {
                log.info("📎 No attachments found for FINANCIAL notification - task ID: {}", task.getId());
            }
        } catch (Exception e) {
            log.error("📎 ❌ FAILED to load attachments for FINANCIAL notification - task ID: {} - Error: {}",
                    task.getId(), e.getMessage(), e);
            taskAttachments = null;
        }

        if (inlineImages != null && !inlineImages.isEmpty()) {
            log.info("📎 Extracted {} inline image(s) from description for FINANCIAL notification - task ID: {} - Files: {}",
                    inlineImages.size(), task.getId(), inlineImages.keySet());
        }

        log.debug("📧 Sending FINANCIAL notification with config - To: {}, CC: {}, Attachments: {}, InlineImages: {}",
                toEmails, ccEmails.isEmpty() ? "none" : ccEmails,
                taskAttachments != null ? taskAttachments.size() : 0,
                inlineImages != null ? inlineImages.size() : 0);

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithAttachments(toEmail, ccRecipientsString, subject, htmlContent, taskAttachments, inlineImages);
                log.debug("Financial notification sent successfully for task ID: {} to {}", task.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send financial notification for task ID: {} to {}: {}",
                        task.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    private void sendFinancialWhatsAppWithNotificationConfig(Task task, List<String> additionalWhatsAppRecipients) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_ORCAMENTO_TAREFA, NotificationType.WHATSAPP);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_ORCAMENTO_TAREFA + WHATSAPP. Task ID: {}", task.getId());
            return;
        }

        List<String> recipients = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            if (task.getRequester() != null && task.getRequester().getPhone() != null
                    && !task.getRequester().getPhone().trim().isEmpty()) {
                recipients.add(task.getRequester().getPhone());
            }
        } else {
            if (config.getPrimaryPhone() != null && !config.getPrimaryPhone().trim().isEmpty()) {
                recipients.add(config.getPrimaryPhone());
            }
        }

        if (config.getPhoneNumbersList() != null && !config.getPhoneNumbersList().isEmpty()) {
            recipients.addAll(config.getPhoneNumbersList());
        }

        if (additionalWhatsAppRecipients != null && !additionalWhatsAppRecipients.isEmpty()) {
            additionalWhatsAppRecipients.stream()
                    .filter(recipient -> recipient != null && !recipient.trim().isEmpty())
                    .forEach(recipients::add);

            log.info("Added {} additional WhatsApp recipient(s) for task ID: {}",
                    additionalWhatsAppRecipients.size(), task.getId());
        }

        if (recipients.isEmpty()) {
            log.warn("No valid WhatsApp recipients found for financial notification. Task ID: {}, Config ID: {}",
                    task.getId(), config.getId());
            return;
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        BigDecimal totalAmount = task.getAmount() != null ? task.getAmount() : BigDecimal.ZERO;

        StringBuilder message = new StringBuilder();
        message.append("*Notificação Automática de Orçamento - DevQuote*\n\n");
        message.append("*📋 Dados da Tarefa*\n\n");
        message.append("*Código:* ").append(task.getCode() != null ? task.getCode() : "N/A").append("\n");
        message.append("*Título:* ").append(task.getTitle() != null ? task.getTitle() : "N/A").append("\n");
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            String cleanDescription = HtmlImageExtractor.stripHtmlTags(task.getDescription());
            if (cleanDescription != null && !cleanDescription.trim().isEmpty()) {
                message.append("*Descrição:*\n").append(cleanDescription).append("\n");
            }
        }
        message.append("*Tipo de Fluxo:* ").append(translateFlowType(task.getFlowType())).append("\n");
        message.append("*Tipo da Tarefa:* ").append(translateTaskType(task.getTaskType())).append("\n");
        if (task.getEnvironment() != null) {
            message.append("*Ambiente:* ").append(translateEnvironment(task.getEnvironment())).append("\n");
        }
        message.append("*Solicitante:* ").append(task.getRequester() != null ? task.getRequester().getName() : "N/A").append("\n");

        if (task.getHasSubTasks()) {
            List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

            if (subTasks != null && !subTasks.isEmpty()) {
                message.append("\n*📋 Dados das Subtarefas*\n\n");

                for (SubTask subTask : subTasks) {
                    message.append("*Título:* ").append(subTask.getTitle() != null ? subTask.getTitle() : "N/A").append("\n");
                    if (subTask.getDescription() != null && !subTask.getDescription().trim().isEmpty()) {
                        String cleanSubDescription = HtmlImageExtractor.stripHtmlTags(subTask.getDescription());
                        if (cleanSubDescription != null && !cleanSubDescription.trim().isEmpty()) {
                            message.append("*Descrição:*\n").append(cleanSubDescription).append("\n");
                        }
                    }
                    BigDecimal subTaskAmount = subTask.getAmount() != null ? subTask.getAmount() : BigDecimal.ZERO;
                    message.append("*Valor:* ").append(currencyFormatter.format(subTaskAmount)).append("\n\n");
                }
            }
        }

        message.append("*Valor Total: ").append(currencyFormatter.format(totalAmount)).append("*\n\n");
        message.append("Para mais detalhes das regras e anexos verifique seu email ou acesse o sistema com seu usuário e senha.\n\n");
        message.append("https://devquote.com.br");

        String finalMessage = message.toString();

        log.debug("📱 Sending FINANCIAL WhatsApp notification - Recipients: {}", recipients);

        for (String recipient : recipients) {
            try {
                whatsAppService.sendMessage(recipient, finalMessage);
                log.debug("Financial WhatsApp notification sent successfully for task ID: {} to {}", task.getId(), recipient);
            } catch (Exception e) {
                log.error("Failed to send financial WhatsApp notification for task ID: {} to {}: {}",
                        task.getId(), recipient, e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendDeliveryNotificationWhatsApp(Delivery delivery, List<String> additionalWhatsAppRecipients) {
        try {
            sendDeliveryWhatsAppWithNotificationConfig(delivery, additionalWhatsAppRecipients);
        } catch (Exception e) {
            log.error("Unexpected error while sending delivery WhatsApp notification for delivery ID: {}: {}",
                    delivery.getId(), e.getMessage(), e);
        }
    }

    private void sendDeliveryWhatsAppWithNotificationConfig(Delivery delivery, List<String> additionalWhatsAppRecipients) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_ENTREGA, NotificationType.WHATSAPP);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_ENTREGA + WHATSAPP. Delivery ID: {}", delivery.getId());
            return;
        }

        List<String> recipients = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            if (delivery.getTask() != null && delivery.getTask().getRequester() != null
                    && delivery.getTask().getRequester().getPhone() != null
                    && !delivery.getTask().getRequester().getPhone().trim().isEmpty()) {
                recipients.add(delivery.getTask().getRequester().getPhone());
            }
        } else {
            if (config.getPrimaryPhone() != null && !config.getPrimaryPhone().trim().isEmpty()) {
                recipients.add(config.getPrimaryPhone());
            }
        }

        if (config.getPhoneNumbersList() != null && !config.getPhoneNumbersList().isEmpty()) {
            recipients.addAll(config.getPhoneNumbersList());
        }

        if (additionalWhatsAppRecipients != null && !additionalWhatsAppRecipients.isEmpty()) {
            additionalWhatsAppRecipients.stream()
                    .filter(recipient -> recipient != null && !recipient.trim().isEmpty())
                    .forEach(recipients::add);

            log.info("Added {} additional WhatsApp recipient(s) for delivery ID: {}",
                    additionalWhatsAppRecipients.size(), delivery.getId());
        }

        if (recipients.isEmpty()) {
            log.warn("No valid WhatsApp recipients found for delivery notification. Delivery ID: {}, Config ID: {}",
                    delivery.getId(), config.getId());
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        StringBuilder message = new StringBuilder();
        message.append("*Notificação Automática de Entrega - DevQuote*\n\n");
        message.append("*📋 Dados da Tarefa*\n\n");
        message.append("*Código:* ").append(delivery.getTask() != null && delivery.getTask().getCode() != null ? delivery.getTask().getCode() : "N/A").append("\n");
        message.append("*Título:* ").append(delivery.getTask() != null && delivery.getTask().getTitle() != null ? delivery.getTask().getTitle() : "N/A").append("\n");
        if (delivery.getTask() != null && delivery.getTask().getDescription() != null && !delivery.getTask().getDescription().trim().isEmpty()) {
            String cleanTaskDesc = HtmlImageExtractor.stripHtmlTags(delivery.getTask().getDescription());
            if (cleanTaskDesc != null && !cleanTaskDesc.trim().isEmpty()) {
                message.append("*Descrição:*\n").append(cleanTaskDesc).append("\n");
            }
        }
        if (delivery.getTask() != null && delivery.getTask().getAmount() != null) {
            message.append("*Valor:* ").append(currencyFormatter.format(delivery.getTask().getAmount())).append("\n");
        }
        message.append("*Tipo de Fluxo:* ").append(delivery.getTask() != null ? translateFlowType(delivery.getTask().getFlowType()) : "N/A").append("\n");
        message.append("*Tipo da Tarefa:* ").append(delivery.getTask() != null ? translateTaskType(delivery.getTask().getTaskType()) : "N/A").append("\n");
        if (delivery.getTask() != null && delivery.getTask().getEnvironment() != null) {
            message.append("*Ambiente:* ").append(translateEnvironment(delivery.getTask().getEnvironment())).append("\n");
        }
        message.append("*Solicitante:* ").append(delivery.getTask() != null && delivery.getTask().getRequester() != null ? delivery.getTask().getRequester().getName() : "N/A").append("\n");

        if (delivery.getNotes() != null && !delivery.getNotes().trim().isEmpty()) {
            String cleanDeliveryNotes = HtmlImageExtractor.stripHtmlTags(delivery.getNotes());
            if (cleanDeliveryNotes != null && !cleanDeliveryNotes.trim().isEmpty()) {
                message.append("\n*Observações:*\n");
                message.append(cleanDeliveryNotes).append("\n");
            }
        }

        if (delivery.getFlowType() == br.com.devquote.enums.FlowType.DESENVOLVIMENTO) {
            if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
                message.append("\n*📋 Itens de Entrega*\n\n");

                for (br.com.devquote.entity.DeliveryItem item : delivery.getItems()) {
                    message.append("*Projeto:* ").append(item.getProject() != null ? item.getProject().getName() : "N/A").append("\n");

                    if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
                        String cleanItemNotes = HtmlImageExtractor.stripHtmlTags(item.getNotes());
                        if (cleanItemNotes != null && !cleanItemNotes.trim().isEmpty()) {
                            message.append("*Observações:*\n").append(cleanItemNotes).append("\n");
                        }
                    }

                    if (item.getPullRequest() != null && !item.getPullRequest().trim().isEmpty()) {
                        message.append("*Link Entrega:* ").append(item.getPullRequest()).append("\n");
                    }

                    if (item.getStartedAt() != null) {
                        message.append("*Data de Início:* ").append(item.getStartedAt().format(dateFormatter)).append("\n");
                    }

                    if (item.getFinishedAt() != null) {
                        message.append("*Data de Conclusão:* ").append(item.getFinishedAt().format(dateFormatter)).append("\n");
                    }

                    message.append("\n");
                }
            }
        } else {
            if (delivery.getOperationalItems() != null && !delivery.getOperationalItems().isEmpty()) {
                message.append("\n*📋 Itens de Entrega*\n\n");

                for (br.com.devquote.entity.DeliveryOperationalItem item : delivery.getOperationalItems()) {
                    message.append("*Título:* ").append(item.getTitle() != null ? item.getTitle() : "N/A").append("\n");

                    if (item.getDescription() != null && !item.getDescription().trim().isEmpty()) {
                        String cleanItemDesc = HtmlImageExtractor.stripHtmlTags(item.getDescription());
                        if (cleanItemDesc != null && !cleanItemDesc.trim().isEmpty()) {
                            message.append("*Descrição:*\n").append(cleanItemDesc).append("\n");
                        }
                    }

                    if (item.getStartedAt() != null) {
                        message.append("*Data de Início:* ").append(item.getStartedAt().format(dateFormatter)).append("\n");
                    }

                    if (item.getFinishedAt() != null) {
                        message.append("*Data de Conclusão:* ").append(item.getFinishedAt().format(dateFormatter)).append("\n");
                    }

                    message.append("\n");
                }
            }
        }

        message.append("Para mais detalhes da entrega verifique seu email ou acesse o sistema com seu usuário e senha.\n\n");
        message.append("https://devquote.com.br");

        String finalMessage = message.toString();

        log.debug("📱 Sending DELIVERY WhatsApp notification - Recipients: {}", recipients);

        for (String recipient : recipients) {
            try {
                whatsAppService.sendMessage(recipient, finalMessage);
                log.debug("Delivery WhatsApp notification sent successfully for delivery ID: {} to {}", delivery.getId(), recipient);
            } catch (Exception e) {
                log.error("Failed to send delivery WhatsApp notification for delivery ID: {} to {}: {}",
                        delivery.getId(), recipient, e.getMessage(), e);
            }
        }
    }

    @Override
    @Async
    @Transactional(readOnly = true)
    public void sendBillingPeriodNotificationAsync(BillingPeriod billingPeriodParam, List<String> additionalEmails, String flowType) {
        if (!emailProperties.isEnabled()) {
            log.warn("Email notifications are disabled. Skipping billing period notification for period ID: {}", billingPeriodParam.getId());
            return;
        }

        try {
            BillingPeriod billingPeriod = billingPeriodRepository.findById(billingPeriodParam.getId())
                    .orElseThrow(() -> new RuntimeException("BillingPeriod not found: " + billingPeriodParam.getId()));

            Context context = new Context();
            context.setVariable("billingPeriod", billingPeriod);

            String[] monthNames = {"janeiro", "fevereiro", "março", "abril", "maio", "junho",
                    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};
            String monthName = monthNames[billingPeriod.getMonth() - 1];

            context.setVariable("monthYear", String.format("%s/%d", monthName, billingPeriod.getYear()));

            context.setVariable("measurementPeriod", String.format("Medição %s de %d", monthName, billingPeriod.getYear()));

            if (billingPeriod.getPaymentDate() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                context.setVariable("paymentDate", billingPeriod.getPaymentDate().format(formatter));
            } else {
                context.setVariable("paymentDate", null);
            }

            List<Object[]> billingTasks = billingPeriodTaskRepository.findTasksWithDetailsByBillingPeriodIdAndFlowType(
                    billingPeriod.getId(),
                    flowType
            );

            List<java.util.Map<String, Object>> tasksData = new java.util.ArrayList<>();
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

            for (Object[] taskData : billingTasks) {
                java.util.Map<String, Object> taskMap = new java.util.HashMap<>();
                taskMap.put("code", taskData[1]);
                taskMap.put("title", taskData[2]);
                taskMap.put("amount", taskData[4]);
                taskMap.put("requesterName", taskData[5]);
                taskMap.put("flowType", taskData[7]);
                taskMap.put("taskType", taskData[8]);
                tasksData.add(taskMap);

                if (taskData[4] != null) {
                    totalAmount = totalAmount.add((java.math.BigDecimal) taskData[4]);
                }
            }

            context.setVariable("tasks", tasksData);
            context.setVariable("totalAmount", totalAmount);
            context.setVariable("taskCount", billingTasks.size());

            String htmlContent = templateEngine.process("email/billing-period-notification", context);
            String subject = "📊 Faturamento Mensal - " + String.format("%02d/%d", billingPeriod.getMonth(), billingPeriod.getYear());

            sendBillingEmailWithNotificationConfig(billingPeriod, subject, htmlContent, additionalEmails);

        } catch (Exception e) {
            log.error("Unexpected error while sending billing period notification for period ID: {}: {}",
                    billingPeriodParam.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send billing period notification email", e);
        }
    }

    private void sendBillingEmailWithNotificationConfig(BillingPeriod billingPeriod, String subject, String htmlContent, List<String> additionalEmails) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_FATURAMENTO, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_FATURAMENTO + EMAIL. BillingPeriod ID: {}", billingPeriod.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            log.warn("Billing period cannot use requester contact - no requester associated. BillingPeriod ID: {}, Config ID: {}",
                    billingPeriod.getId(), config.getId());
            return;
        } else {

            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {

            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for billing period notification. BillingPeriod ID: {}",
                    additionalEmails.size(), billingPeriod.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for billing notification. BillingPeriod ID: {}, Config ID: {}",
                    billingPeriod.getId(), config.getId());
            return;
        }

        log.debug("📧 Sending BILLING notification with config - To: {}, CC: {}",
                toEmails, ccEmails.isEmpty() ? "none" : ccEmails);

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithAttachments(toEmail, ccRecipientsString, subject, htmlContent, null);
                log.debug("Billing notification sent successfully for period ID: {} to {}", billingPeriod.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send billing notification for period ID: {} to {}: {}",
                        billingPeriod.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    @Override
    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendDeliveryUpdatedNotificationWithAttachmentData(Delivery deliveryParam, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails) {
        if (deliveryParam == null) {
            log.warn("Cannot send delivery updated notification with attachments: delivery is null");
            return;
        }

        try {
            Delivery delivery = deliveryRepository.findById(deliveryParam.getId())
                    .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryParam.getId()));

            String subject = String.format("📊 Dados da Entrega - %s",
                    delivery.getTask() != null && delivery.getTask().getCode() != null ?
                            delivery.getTask().getCode() : "Código não disponível");

            String htmlContent = buildDeliveryUpdatedEmailContent(delivery);

            sendToMultipleRecipientsForDeliveryWithInMemoryAttachments(delivery, subject, htmlContent, "updated", attachmentDataMap, additionalEmails);

        } catch (Exception e) {
            log.error("Failed to send delivery updated notification with attachments for delivery ID: {}", deliveryParam.getId(), e);
        }
    }

    private void sendToMultipleRecipientsForDeliveryWithInMemoryAttachments(Delivery delivery, String subject, String htmlContent, String action, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_ENTREGA, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_ENTREGA + EMAIL. Delivery ID: {}", delivery.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {

            if (delivery.getTask() != null && delivery.getTask().getRequester() != null
                    && delivery.getTask().getRequester().getEmail() != null
                    && !delivery.getTask().getRequester().getEmail().trim().isEmpty()) {
                toEmails.add(delivery.getTask().getRequester().getEmail());
            }
        } else {

            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {

            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for delivery notification with attachments. Delivery ID: {}",
                    additionalEmails.size(), delivery.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for delivery notification with attachments. Delivery ID: {}, Config ID: {}",
                    delivery.getId(), config.getId());
            return;
        }

        log.debug("📧 Sending DELIVERY {} notification WITH IN-MEMORY ATTACHMENTS with config - To: {}, CC: {}",
                action.toUpperCase(), toEmails, ccEmails.isEmpty() ? "none" : ccEmails);

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithInMemoryAttachments(toEmail, ccRecipientsString, subject, htmlContent, attachmentDataMap);
                log.debug("Delivery notification with attachments sent successfully for delivery ID: {} to {}", delivery.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send delivery notification with attachments for delivery ID: {} to {}: {}",
                        delivery.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    @Override
    @Async("emailTaskExecutor")
    @Transactional(readOnly = true)
    public void sendBillingPeriodNotificationWithAttachmentData(BillingPeriod billingPeriodParam, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails, String flowType) {
        if (billingPeriodParam == null) {
            log.warn("Cannot send billing period notification with attachments: billingPeriod is null");
            return;
        }

        try {
            BillingPeriod billingPeriod = billingPeriodRepository.findById(billingPeriodParam.getId())
                    .orElseThrow(() -> new RuntimeException("BillingPeriod not found: " + billingPeriodParam.getId()));

            String[] monthNames = {"janeiro", "fevereiro", "março", "abril", "maio", "junho",
                    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};
            String monthName = monthNames[billingPeriod.getMonth() - 1];
            String subject = String.format("Medição %s de %d - DevQuote", monthName, billingPeriod.getYear());

            Context context = new Context();
            context.setVariable("billingPeriod", billingPeriod);

            context.setVariable("monthYear", String.format("%s/%d", monthName, billingPeriod.getYear()));

            context.setVariable("measurementPeriod", String.format("Medição %s de %d", monthName, billingPeriod.getYear()));

            if (billingPeriod.getPaymentDate() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                context.setVariable("paymentDate", billingPeriod.getPaymentDate().format(formatter));
            } else {
                context.setVariable("paymentDate", null);
            }

            List<Object[]> billingTasks = billingPeriodTaskRepository.findTasksWithDetailsByBillingPeriodIdAndFlowType(
                    billingPeriod.getId(),
                    flowType
            );

            List<java.util.Map<String, Object>> tasksData = new java.util.ArrayList<>();
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

            for (Object[] taskData : billingTasks) {
                java.util.Map<String, Object> taskMap = new java.util.HashMap<>();
                taskMap.put("code", taskData[1]);
                taskMap.put("title", taskData[2]);
                taskMap.put("amount", taskData[4]);
                taskMap.put("requesterName", taskData[5]);
                taskMap.put("flowType", taskData[7]);
                taskMap.put("taskType", taskData[8]);
                tasksData.add(taskMap);

                if (taskData[4] != null) {
                    totalAmount = totalAmount.add((java.math.BigDecimal) taskData[4]);
                }
            }

            context.setVariable("tasks", tasksData);
            context.setVariable("totalAmount", totalAmount);
            context.setVariable("taskCount", billingTasks.size());

            String htmlContent = templateEngine.process("email/billing-period-notification", context);

            sendBillingEmailWithAttachmentsUsingNotificationConfig(billingPeriod, subject, htmlContent, attachmentDataMap, additionalEmails);

        } catch (Exception e) {
            log.error("Failed to send billing period notification with attachments for period ID: {}",
                    billingPeriodParam.getId(), e);
        }
    }

    private void sendBillingEmailWithAttachmentsUsingNotificationConfig(BillingPeriod billingPeriod, String subject, String htmlContent, Map<String, byte[]> attachmentDataMap, List<String> additionalEmails) {
        NotificationConfig config = findNotificationConfig(NotificationConfigType.NOTIFICACAO_FATURAMENTO, NotificationType.EMAIL);

        if (config == null) {
            log.warn("No notification config found for NOTIFICACAO_FATURAMENTO + EMAIL. BillingPeriod ID: {}", billingPeriod.getId());
            return;
        }

        List<String> toEmails = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();

        if (Boolean.TRUE.equals(config.getUseRequesterContact())) {
            log.warn("Billing period cannot use requester contact - no requester associated. BillingPeriod ID: {}, Config ID: {}",
                    billingPeriod.getId(), config.getId());
            return;
        } else {

            if (config.getPrimaryEmail() != null && !config.getPrimaryEmail().trim().isEmpty()) {
                toEmails.add(config.getPrimaryEmail());
            }
        }

        if (config.getCopyEmailsList() != null && !config.getCopyEmailsList().isEmpty()) {
            ccEmails.addAll(config.getCopyEmailsList());
        }

        if (additionalEmails != null && !additionalEmails.isEmpty()) {

            additionalEmails.stream()
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .filter(email -> email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
                    .forEach(ccEmails::add);

            log.info("Added {} additional email(s) to CC list for billing period notification with attachments. BillingPeriod ID: {}",
                    additionalEmails.size(), billingPeriod.getId());
        }

        if (toEmails.isEmpty()) {
            log.warn("No valid recipients found for billing notification with attachments. BillingPeriod ID: {}, Config ID: {}",
                    billingPeriod.getId(), config.getId());
            return;
        }

        for (String toEmail : toEmails) {
            try {
                String ccRecipientsString = ccEmails.isEmpty() ? null : String.join(",", ccEmails);
                sendEmailWithInMemoryAttachments(toEmail, ccRecipientsString, subject, htmlContent, attachmentDataMap);
                log.debug("Billing notification with attachments sent successfully for period ID: {} to {}", billingPeriod.getId(), toEmail);
            } catch (Exception e) {
                log.error("Failed to send billing notification with attachments for period ID: {} to {}: {}",
                        billingPeriod.getId(), toEmail, e.getMessage(), e);
            }
        }
    }

    private String convertLineBreaksToHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return text.replace("\n", "<br>").replace("\r\n", "<br>").replace("\r", "<br>");
    }

    private static final java.util.regex.Pattern IMG_TAG_PATTERN = java.util.regex.Pattern.compile(
            "<img[^>]*src=[\"'](?:https?://[^/]+)?/api/inline-images/view/([^\"']+)[\"'][^>]*>",
            java.util.regex.Pattern.CASE_INSENSITIVE
    );

    private String determineMimeType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "image/jpeg";
    }

    private String getFileExtension(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".png")) return ".png";
        if (lowerPath.endsWith(".gif")) return ".gif";
        if (lowerPath.endsWith(".webp")) return ".webp";
        if (lowerPath.endsWith(".svg")) return ".svg";
        if (lowerPath.endsWith(".jpg")) return ".jpg";
        return ".png";
    }

    private String prepareHtmlForEmail(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return replaceImagesWithPlaceholders(html, new int[]{1});
    }

    private String prepareHtmlForEmailWithCounter(String html, int[] counter) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return replaceImagesWithPlaceholders(html, counter);
    }

    private String replaceImagesWithPlaceholders(String html, int[] counter) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        java.util.regex.Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String placeholder = String.format("<strong>[imagem-%02d]</strong>", counter[0]++);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(placeholder));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public Map<String, byte[]> extractInlineImages(String html) {
        return extractInlineImagesWithCounter(html, new int[]{1});
    }

    public Map<String, byte[]> extractInlineImagesWithCounter(String html, int[] counter) {
        Map<String, byte[]> images = new java.util.LinkedHashMap<>();
        if (html == null || html.isEmpty()) {
            return images;
        }

        java.util.regex.Matcher matcher = IMG_TAG_PATTERN.matcher(html);

        while (matcher.find()) {
            String filePath = matcher.group(1);
            try {
                java.io.InputStream inputStream = fileStorageStrategy.getFileStream(filePath);
                byte[] imageBytes = inputStream.readAllBytes();
                inputStream.close();

                String extension = getFileExtension(filePath);
                String imageName = String.format("imagem-%02d%s", counter[0]++, extension);
                images.put(imageName, imageBytes);
                log.info("Extracted inline image for email attachment: {} ({} bytes)", imageName, imageBytes.length);
            } catch (Exception e) {
                log.warn("Failed to extract inline image: {}, error: {}", filePath, e.getMessage());
                counter[0]++;
            }
        }

        return images;
    }
}
