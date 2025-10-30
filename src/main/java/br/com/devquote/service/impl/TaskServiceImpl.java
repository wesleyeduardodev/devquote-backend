package br.com.devquote.service.impl;
import br.com.devquote.adapter.BillingPeriodAdapter;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.adapter.TaskAdapter;
import br.com.devquote.enums.FlowType;
import br.com.devquote.error.BusinessException;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.configuration.BillingProperties;
import br.com.devquote.dto.request.*;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import br.com.devquote.dto.response.TaskResponse;
import br.com.devquote.dto.response.TaskWithSubTasksResponse;
import br.com.devquote.entity.*;
import br.com.devquote.repository.RequesterRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.*;
import br.com.devquote.utils.ExcelReportUtils;
import br.com.devquote.utils.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final RequesterRepository requesterRepository;
    private final SubTaskRepository subTaskRepository;
    private final BillingPeriodService billingPeriodService;
    private final BillingPeriodTaskService billingPeriodTaskService;
    private final BillingProperties billingProperties;
    private final SecurityUtils securityUtils;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;
    private final EmailService emailService;
    private final DeliveryService deliveryService;
    private final TaskAttachmentService taskAttachmentService;
    private final br.com.devquote.service.storage.FileStorageStrategy fileStorageStrategy;

    @Override
    public List<TaskResponse> findAll() {
        List<TaskResponse> tasks = taskRepository.findAllOrderedById().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());

        if (!tasks.isEmpty()) {
            List<Long> taskIds = tasks.stream().map(TaskResponse::getId).toList();

            // Buscar informações de Billing e Delivery
            Map<Long, Boolean> taskInBillingMap = getTaskInBillingStatus(taskIds);
            Map<Long, Boolean> taskDeliveryMap = getTaskDeliveryStatus(taskIds);

            tasks.forEach(dto -> {
                List<SubTask> subTasks = subTaskRepository.findByTaskId(dto.getId());
                dto.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));

                // Adicionar informações de Billing e Delivery
                dto.setHasQuote(false); // Não há mais quotes
                dto.setHasQuoteInBilling(taskInBillingMap.getOrDefault(dto.getId(), false));
                dto.setHasDelivery(taskDeliveryMap.getOrDefault(dto.getId(), false));
            });
        }

        return tasks;
    }

    @Override
    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        TaskResponse response = TaskAdapter.toResponseDTO(task);
        List<SubTask> subTasks = subTaskRepository.findByTaskId(response.getId());
        response.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));

        // Adicionar informações de Billing e Delivery para o item específico
        response.setHasQuote(false); // Não há mais quotes
        response.setHasQuoteInBilling(billingPeriodTaskService.existsByTaskId(id));
        response.setHasDelivery(deliveryService.existsByTaskId(id));

        return response;
    }

    @Override
    public TaskResponse create(TaskRequest dto) {
        validateCreatePermission();

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Usuário não autenticado", "USER_NOT_AUTHENTICATED");
        }

        log.debug("TASK CREATE requesterId={} title={} user={} flowType={}",
            dto.getRequesterId(), dto.getTitle(), currentUser.getUsername(), dto.getFlowType());

        FlowType flowType = FlowType.fromString(dto.getFlowType());

        if (flowType == FlowType.OPERACIONAL) {
            String generatedCode = generateRandomCode();
            dto.setCode(generatedCode);
            log.debug("Generated code for OPERACIONAL task: {}", generatedCode);
        } else {
            if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
                throw new BusinessException("Código é obrigatório para tarefas de desenvolvimento", "CODE_REQUIRED");
            }
            if (taskRepository.existsByCode(dto.getCode())) {
                throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
            }
        }

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", dto.getRequesterId()));

        Task entity = TaskAdapter.toEntity(dto, requester);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);

        try {
            entity = taskRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("uk_task_code") || errorMessage.contains("code"))) {
                throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
            }
            throw new BusinessException("Erro ao salvar tarefa: " + e.getMessage(), "TASK_SAVE_ERROR");
        }

        // Criar entrega automaticamente para a nova tarefa
        ensureTaskHasDelivery(entity);

        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public TaskResponse update(Long id, TaskRequest dto) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        validateTaskAccess(entity, "editar");

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Usuário não autenticado", "USER_NOT_AUTHENTICATED");
        }

        // Verificar se o código já existe em outra tarefa ANTES de tentar salvar
        if (taskRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
        }

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", dto.getRequesterId()));

        TaskAdapter.updateEntityFromDto(dto, entity, requester);
        entity.setUpdatedBy(currentUser);

        try {
            entity = taskRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("uk_task_code") || errorMessage.contains("code"))) {
                throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
            }
            throw new BusinessException("Erro ao salvar tarefa: " + e.getMessage(), "TASK_SAVE_ERROR");
        }

        // Verificar e criar entrega se não existir
        ensureTaskHasDelivery(entity);

        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        Task entity = taskRepository.findById(id)
                .map(task -> {
                    // Inicializar relacionamentos lazy antes do envio assíncrono
                    if (task.getRequester() != null) {
                        task.getRequester().getName();
                        task.getRequester().getEmail();
                    }
                    if (task.getCreatedBy() != null) {
                        task.getCreatedBy().getUsername();
                        task.getCreatedBy().getName();
                    }
                    if (task.getUpdatedBy() != null) {
                        task.getUpdatedBy().getUsername();
                        task.getUpdatedBy().getName();
                    }
                    return task;
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(entity, "excluir");

        // ESTRATÉGIA ROBUSTA: Tentar baixar anexos, se conseguir incluir no email, se não conseguir enviar só os dados da tarefa
        Map<String, byte[]> attachmentDataMap = new HashMap<>();
        boolean hasAttachments = false;
        
        try {
            List<TaskAttachment> attachments = taskAttachmentService.getTaskAttachmentsEntities(id);
            if (attachments != null && !attachments.isEmpty()) {
                hasAttachments = true;
                
                for (TaskAttachment attachment : attachments) {
                    try {
                        // Tentar baixar do S3
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                            
                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put(attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                        // Continua tentando outros anexos
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing attachments from database: {}", e.getMessage());
        }

        // STEP 2/3 - ENVIAR EMAIL (com anexos se conseguiu baixar, sem anexos se não conseguiu)
        // DESABILITADO: Não enviar mais email na exclusão de tarefa
        /*
        try {
            if (attachmentDataMap.isEmpty() && hasAttachments) {
                // Tinha anexos mas não conseguiu baixar nenhum - enviar email simples
                emailService.sendTaskDeletedNotification(entity);
            } else if (!attachmentDataMap.isEmpty()) {
                // Conseguiu baixar anexos - enviar email com anexos
                emailService.sendTaskDeletedNotificationWithAttachmentData(entity, attachmentDataMap);
            } else {
                // Não tinha anexos - enviar email simples
                emailService.sendTaskDeletedNotification(entity);
            }
        } catch (Exception e) {
            log.error("FAILED to send deletion email for task ID: {} - Error: {}", id, e.getMessage());
            // Não impede a exclusão da tarefa
        }
        */

        // STEP 3/3 - DELETAR TAREFA E ANEXOS (sempre acontece, mesmo se email falhou)
        try {
            taskAttachmentService.deleteAllTaskAttachmentsAndFolder(id);
        } catch (Exception e) {
            log.error("Failed to delete attachments folder for task {}: {}", id, e.getMessage());
        }

        taskRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        User currentUser = securityUtils.getCurrentUser();
        log.warn("TASK BULK DELETE count={} user={}",
            ids.size(), currentUser != null ? currentUser.getUsername() : "unknown");

        for (Long id : ids) {
            deleteTaskWithSubTasks(id);
        }
    }

    @Override
    public TaskWithSubTasksResponse createWithSubTasks(TaskWithSubTasksCreateRequest dto) {
        validateCreatePermission();

        FlowType flowType = FlowType.fromString(dto.getFlowType());

        if (flowType == FlowType.OPERACIONAL) {
            String generatedCode = generateRandomCode();
            dto.setCode(generatedCode);
            log.debug("Generated code for OPERACIONAL task: {}", generatedCode);
        } else {
            if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
                throw new BusinessException("Código é obrigatório para tarefas de desenvolvimento", "CODE_REQUIRED");
            }
            if (taskRepository.existsByCode(dto.getCode())) {
                throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
            }
        }

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Task task = buildAndPersistTask(dto, requester);

        List<SubTask> persistedSubTasks = persistSubTasks(task, dto.getSubTasks());

        // Processa o valor da tarefa conforme nova lógica
        processTaskAmount(task, persistedSubTasks);
        task = taskRepository.save(task);

        // Criar entrega automaticamente para a nova tarefa
        ensureTaskHasDelivery(task);

        return buildTaskWithSubTasksResponse(task, persistedSubTasks);
    }


    @Override
    public TaskWithSubTasksResponse updateWithSubTasks(Long taskId, TaskWithSubTasksUpdateRequest dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(task, "editar");

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (!dto.getCode().equals(task.getCode()) && taskRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("Já existe uma tarefa com o código '" + dto.getCode() + "'. Por favor, use um código diferente.", "DUPLICATE_TASK_CODE");
        }

        Boolean newHasSubTasks = dto.getHasSubTasks() != null ? dto.getHasSubTasks() : false;
        validateCanRemoveSubTasksFlag(task, newHasSubTasks);

        TaskAdapter.updateEntityFromDto(TaskRequest.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .flowType(dto.getFlowType())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .hasSubTasks(newHasSubTasks)
                .amount(dto.getAmount())
                .taskType(dto.getTaskType())
                .serverOrigin(dto.getServerOrigin())
                .systemModule(dto.getSystemModule())
                .priority(dto.getPriority())
                .build(), task, requester);

        task.setUpdatedBy(currentUser);

        List<SubTask> updated = upsertAndDeleteSubTasks(task, dto.getSubTasks());

        // Processa o valor da tarefa conforme nova lógica
        processTaskAmount(task, updated);
        task = taskRepository.save(task);

        // Verificar e criar entrega se não existir
        ensureTaskHasDelivery(task);

        return buildTaskWithSubTasksResponse(task, updated);
    }

    @Override
    public void deleteTaskWithSubTasks(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .map(entity -> {
                    // Inicializar relacionamentos lazy antes do envio assíncrono
                    if (entity.getRequester() != null) {
                        entity.getRequester().getName();
                        entity.getRequester().getEmail();
                    }
                    if (entity.getCreatedBy() != null) {
                        entity.getCreatedBy().getUsername();
                        entity.getCreatedBy().getName();
                    }
                    if (entity.getUpdatedBy() != null) {
                        entity.getUpdatedBy().getUsername();
                        entity.getUpdatedBy().getName();
                    }
                    return entity;
                })
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(task, "excluir");

        if (billingPeriodTaskService.existsByTaskId(taskId)) {
            throw new RuntimeException("Cannot delete task. It is linked to a billing period.");
        }

        // ESTRATÉGIA ROBUSTA: Tentar baixar anexos, se conseguir incluir no email, se não conseguir enviar só os dados da tarefa
        Map<String, byte[]> attachmentDataMap = new HashMap<>();
        boolean hasAttachments = false;
        
        try {
            List<TaskAttachment> attachments = taskAttachmentService.getTaskAttachmentsEntities(taskId);
            if (attachments != null && !attachments.isEmpty()) {
                hasAttachments = true;
                
                for (TaskAttachment attachment : attachments) {
                    try {
                        // Tentar baixar do S3
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                            
                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put(attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                        // Continua tentando outros anexos
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing attachments from database: {}", e.getMessage());
        }

        // STEP 2/3 - ENVIAR EMAIL (com anexos se conseguiu baixar, sem anexos se não conseguiu)
        // DESABILITADO: Não enviar mais email na exclusão de tarefa
        /*
        try {
            if (attachmentDataMap.isEmpty() && hasAttachments) {
                // Tinha anexos mas não conseguiu baixar nenhum - enviar email simples
                emailService.sendTaskDeletedNotification(task);
            } else if (!attachmentDataMap.isEmpty()) {
                // Conseguiu baixar anexos - enviar email com anexos
                emailService.sendTaskDeletedNotificationWithAttachmentData(task, attachmentDataMap);
            } else {
                // Não tinha anexos - enviar email simples
                emailService.sendTaskDeletedNotification(task);
            }
        } catch (Exception e) {
            log.error("FAILED to send deletion email for task ID: {} - Error: {}", taskId, e.getMessage());
            // Não impede a exclusão da tarefa
        }
        */

        // STEP 3/3 - DELETAR TAREFA E ANEXOS (sempre acontece, mesmo se email falhou)
        try {
            taskAttachmentService.deleteAllTaskAttachmentsAndFolder(taskId);
        } catch (Exception e) {
            log.error("Failed to delete attachments folder for task {}: {}", taskId, e.getMessage());
        }

        subTaskRepository.deleteByTaskId(taskId);
        taskRepository.deleteById(taskId);
    }

    @Override
    public Page<TaskResponse> findAllPaginated(Long id,
                                               Long requesterId,
                                               String requesterName,
                                               String title,
                                               String description,
                                               String code,
                                               String link,
                                               String createdAt,
                                               String updatedAt,
                                               FlowType flowType,
                                               Pageable pageable) {

        // Todos os usuários podem ver todas as tarefas
        Page<Task> page = taskRepository.findByOptionalFieldsPaginated(
                id, requesterId, requesterName, title, description, code, link, createdAt, updatedAt, flowType, pageable
        );
        return buildTaskResponsePage(page, pageable);
    }

    @Override
    public Page<TaskResponse> findUnlinkedBillingByOptionalFieldsPaginated(Long id,
                                                                           Long requesterId,
                                                                           String requesterName,
                                                                           String title,
                                                                           String description,
                                                                           String code,
                                                                           String link,
                                                                           String createdAt,
                                                                           String updatedAt,
                                                                           FlowType flowType,
                                                                           Pageable pageable) {

        Page<Task> page = taskRepository.findUnlinkedBillingByOptionalFieldsPaginated(
                id, requesterId, requesterName, title, description, code, link, createdAt, updatedAt, flowType, pageable
        );
        return buildTaskResponsePage(page, pageable);
    }

    @Override
    public Page<TaskResponse> findUnlinkedDeliveryByOptionalFieldsPaginated(Long id,
                                                                            Long requesterId,
                                                                            String requesterName,
                                                                            String title,
                                                                            String description,
                                                                            String code,
                                                                            String link,
                                                                            String createdAt,
                                                                            String updatedAt,
                                                                            FlowType flowType,
                                                                            Pageable pageable) {

        Page<Task> page = taskRepository.findUnlinkedDeliveryByOptionalFieldsPaginated(
                id, requesterId, requesterName, title, description, code, link, createdAt, updatedAt, flowType, pageable
        );
        return buildTaskResponsePage(page, pageable);
    }

    private Page<TaskResponse> buildTaskResponsePage(Page<Task> page, Pageable pageable) {
        List<TaskResponse> dtos = page.getContent().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());

        if (dtos.isEmpty()) {
            return new PageImpl<>(dtos, pageable, page.getTotalElements());
        }

        List<Long> taskIds = dtos.stream().map(TaskResponse::getId).toList();
        List<SubTask> allSubTasks = subTaskRepository.findByTaskIdIn(taskIds);

        Map<Long, List<SubTask>> subTasksByTaskId = allSubTasks.stream()
                .collect(Collectors.groupingBy(st -> st.getTask().getId()));

        // Buscar informações de Billing e Delivery
        Map<Long, Boolean> taskInBillingMap = getTaskInBillingStatus(taskIds);
        Map<Long, Boolean> taskDeliveryMap = getTaskDeliveryStatus(taskIds);

        dtos.forEach(dto -> {
            List<SubTask> list = subTasksByTaskId.getOrDefault(dto.getId(), List.of());
            dto.setSubTasks(SubTaskAdapter.toResponseDTOList(list));

            // Adicionar informações de Billing e Delivery
            dto.setHasQuote(false); // Não há mais quotes
            dto.setHasQuoteInBilling(taskInBillingMap.getOrDefault(dto.getId(), false));
            dto.setHasDelivery(taskDeliveryMap.getOrDefault(dto.getId(), false));
        });

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private Task buildAndPersistTask(TaskWithSubTasksCreateRequest dto, Requester requester) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Task task = TaskAdapter.toEntity(TaskRequest.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .flowType(dto.getFlowType())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .hasSubTasks(dto.getHasSubTasks())
                .amount(dto.getAmount())
                .taskType(dto.getTaskType())
                .serverOrigin(dto.getServerOrigin())
                .systemModule(dto.getSystemModule())
                .priority(dto.getPriority())
                .build(), requester);

        task.setCreatedBy(currentUser);
        task.setUpdatedBy(currentUser);

        return taskRepository.save(task);
    }

    private List<SubTask> persistSubTasks(Task task, List<SubTaskRequest> subTasks) {
        List<SubTaskRequest> safe = subTasks == null ? List.of() : subTasks.stream()
                .filter(Objects::nonNull)
                .toList();

        return safe.stream()
                .map(stDto -> subTaskRepository.save(SubTaskAdapter.toEntity(stDto, task)))
                .toList();
    }



    private BillingPeriod getOrCreateBillingPeriod(LocalDate baseDate) {
        int year = baseDate.getYear();
        int month = baseDate.getMonthValue();

        BillingPeriod found = billingPeriodService.findByYearAndMonth(year, month);
        if (found != null) return found;

        LocalDate paymentDate = computeNextMonthPaymentDate(baseDate, billingProperties.getPaymentDay());

        BillingPeriodRequest createDto = BillingPeriodRequest.builder()
                .year(year)
                .month(month)
                .paymentDate(paymentDate)
                .build();

        BillingPeriodResponse created = billingPeriodService.create(createDto);
        return BillingPeriodAdapter.toEntity(created);
    }

    private LocalDate computeNextMonthPaymentDate(LocalDate base, int desiredDay) {
        LocalDate nextMonthFirst = base.plusMonths(1).withDayOfMonth(1);
        int day = Math.min(Math.max(desiredDay, 1), nextMonthFirst.lengthOfMonth());
        return nextMonthFirst.withDayOfMonth(day);
    }

    private void ensureTaskLinkedToBillingMonth(Long taskId, Long billingMonthId) {
        if (billingPeriodTaskService.existsByTaskId(taskId)) {
            // Verificar se já está no período correto
            Optional<BillingPeriodTaskResponse> existingLink = billingPeriodTaskService.findByTaskId(taskId);
            if (existingLink.isPresent() && existingLink.get().getBillingPeriodId().equals(billingMonthId)) {
                return; // Já está vinculada ao período correto
            }
            // Se está em outro período, não fazer nada - deixar a validação do service tratar
        }

        BillingPeriodTaskRequest linkReq = BillingPeriodTaskRequest.builder()
                .taskId(taskId)
                .billingPeriodId(billingMonthId)
                .build();

        billingPeriodTaskService.create(linkReq);
    }

    private TaskWithSubTasksResponse buildTaskWithSubTasksResponse(Task task, List<SubTask> subTasks) {
        return TaskWithSubTasksResponse.builder()
                .id(task.getId())
                .requesterId(task.getRequester().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .code(task.getCode())
                .link(task.getLink())
                .meetingLink(task.getMeetingLink())
                .hasSubTasks(task.getHasSubTasks())
                .amount(task.getAmount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdByUserId(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .createdByUserName(task.getCreatedBy() != null ? task.getCreatedBy().getName() : null)
                .updatedByUserId(task.getUpdatedBy() != null ? task.getUpdatedBy().getId() : null)
                .updatedByUserName(task.getUpdatedBy() != null ? task.getUpdatedBy().getName() : null)
                .subTasks(SubTaskAdapter.toResponseDTOList(subTasks))
                .build();
    }

    private List<SubTask> upsertAndDeleteSubTasks(Task task, List<SubTaskUpdateRequest> subTaskDtos) {
        List<SubTaskUpdateRequest> safe = subTaskDtos == null ? List.of() : subTaskDtos;

        List<SubTask> upserted = safe.stream()
                .filter(st -> !st.isExcluded())
                .map(st -> {
                    if (st.getId() != null) {
                        SubTask entity = subTaskRepository.findById(st.getId())
                                .orElseThrow(() -> new RuntimeException("SubTask not found: " + st.getId()));
                        SubTaskAdapter.updateEntityFromDto(st, entity, task);
                        return subTaskRepository.save(entity);
                    }
                    return subTaskRepository.save(SubTaskAdapter.toEntity(st, task));
                })
                .toList();

        safe.stream()
                .filter(st -> st.isExcluded() && st.getId() != null)
                .forEach(st -> subTaskRepository.deleteById(st.getId()));

        return upserted;
    }

    private boolean isTrue(Boolean flag) {
        return Boolean.TRUE.equals(flag);
    }

    private void validateCreatePermission() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        var profiles = currentUser.getActiveProfileCodes();
        if (!profiles.contains("ADMIN") && !profiles.contains("MANAGER") && !profiles.contains("USER")) {
            throw new RuntimeException("Usuário não possui permissão para criar tarefas");
        }
    }

    private void validateTaskAccess(Task task, String action) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        var profiles = currentUser.getActiveProfileCodes();

        // ADMIN tem acesso total
        if (profiles.contains("ADMIN")) {
            return;
        }

        // MANAGER e USER podem acessar apenas suas próprias tarefas
        if (profiles.contains("MANAGER") || profiles.contains("USER")) {
            if (task.getCreatedBy() == null || !task.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new RuntimeException(String.format("Você não possui permissão para %s esta tarefa. Apenas o usuário que criou a tarefa pode %s.", action, action));
            }
            return;
        }

        throw new RuntimeException("Usuário não possui permissão para acessar tarefas");
    }

    /**
     * Processa a tarefa conforme a nova lógica:
     * - Se hasSubTasks = true: calcula amount como soma das subtarefas
     * - Se hasSubTasks = false: usa amount diretamente informado
     */
    private void processTaskAmount(Task task, List<SubTask> subTasks) {
        if (Boolean.TRUE.equals(task.getHasSubTasks())) {
            // Calcula soma das subtarefas
            BigDecimal totalAmount = subTasks.stream()
                    .filter(Objects::nonNull)
                    .map(SubTask::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            task.setAmount(totalAmount);
        }
        // Se hasSubTasks = false, mantém o amount já informado
    }

    /**
     * Valida se é possível desmarcar a flag hasSubTasks
     */
    private void validateCanRemoveSubTasksFlag(Task task, boolean newHasSubTasks) {
        // Se estava com subtarefas e agora quer desmarcar
        if (Boolean.TRUE.equals(task.getHasSubTasks()) && !newHasSubTasks) {
            List<SubTask> existingSubTasks = subTaskRepository.findByTaskId(task.getId());
            if (!existingSubTasks.isEmpty()) {
                throw new RuntimeException("Não é possível desmarcar 'Tem Subtarefas' enquanto existirem subtarefas vinculadas. Remova todas as subtarefas primeiro.");
            }
        }
    }

    /**
     * Verifica quais tarefas estão vinculadas ao faturamento
     */
    private Map<Long, Boolean> getTaskInBillingStatus(List<Long> taskIds) {
        return taskIds.stream()
                .collect(Collectors.toMap(
                        taskId -> taskId,
                        taskId -> billingPeriodTaskService.existsByTaskId(taskId)
                ));
    }

    /**
     * Verifica quais tarefas estão vinculadas a entregas
     */
    private Map<Long, Boolean> getTaskDeliveryStatus(List<Long> taskIds) {
        return taskIds.stream()
                .collect(Collectors.toMap(
                        taskId -> taskId,
                        taskId -> deliveryService.existsByTaskId(taskId)
                ));
    }

    @Override
    public byte[] exportTasksToExcel() throws IOException {
        log.debug("EXCEL EXPORT STARTED");

        // Verificar perfil do usuário para controle de colunas de valor
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Usuário não autenticado", "USER_NOT_AUTHENTICATED");
        }

        var profiles = currentUser.getActiveProfileCodes();
        boolean canViewAmounts = profiles.contains("ADMIN") || profiles.contains("MANAGER");
        log.debug("EXCEL EXPORT user={} canViewAmounts={}", currentUser.getUsername(), canViewAmounts);

        // Consulta nativa SQL para obter todos os dados de tarefas e subtarefas
        String sql = """
            SELECT 
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.task_type as task_type,
                t.flow_type as task_flow_type,
                t.priority as task_priority,
                r.name as requester_name,
                cb.username as created_by_user,
                ub.username as updated_by_user,
                t.server_origin,
                t.system_module,
                t.link as task_link,
                t.meeting_link,
                t.amount as task_amount,
                t.has_sub_tasks as has_subtasks,
                CASE WHEN EXISTS(SELECT 1 FROM delivery d WHERE d.task_id = t.id) THEN 'Sim' ELSE 'Não' END as has_delivery,
                CASE WHEN EXISTS(SELECT 1 FROM billing_period_task bpt 
                                 WHERE bpt.task_id = t.id) THEN 'Sim' ELSE 'Não' END as has_quote_in_billing,
                t.created_at as task_created_at,
                t.updated_at as task_updated_at,
                st.id as subtask_id,
                st.title as subtask_title,
                st.description as subtask_description,
                st.amount as subtask_amount
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN sub_task st ON st.task_id = t.id
            ORDER BY t.id desc, st.id
            """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // Converter resultados para Map
        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_description", row[3]);
            map.put("task_type", row[4]);
            map.put("task_flow_type", row[5]);
            map.put("task_priority", row[6]);
            map.put("requester_name", row[7]);
            map.put("created_by_user", row[8]);
            map.put("updated_by_user", row[9]);
            map.put("server_origin", row[10]);
            map.put("system_module", row[11]);
            map.put("task_link", row[12]);
            map.put("meeting_link", row[13]);
            map.put("task_amount", row[14]);
            map.put("has_subtasks", Boolean.TRUE.equals(row[15]) ? "Sim" : "Não");
            map.put("has_delivery", row[16]);
            map.put("has_quote_in_billing", row[17]);
            map.put("task_created_at", row[18]);
            map.put("task_updated_at", row[19]);
            map.put("subtask_id", row[20]);
            map.put("subtask_title", row[21]);
            map.put("subtask_description", row[22]);
            map.put("subtask_amount", row[23]);
            return map;
        }).collect(Collectors.toList());

        log.debug("EXCEL EXPORT generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateTasksReport(data, canViewAmounts);
        log.debug("EXCEL EXPORT completed successfully");

        return result;
    }

    @Override
    public byte[] exportGeneralReport() throws IOException {
        log.debug("GENERAL REPORT EXPORT STARTED");

        // Query incluindo entregas e faturamento: Tarefas → Entregas → Faturamento
        String sql = """
            SELECT 
                -- DADOS DA TAREFA
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.priority as task_priority,
                t.amount as task_amount,
                r.name as requester_name,
                
                -- METADADOS DA TAREFA
                cb.username as created_by_name,
                ub.username as updated_by_name,
                t.server_origin as task_server_origin,
                t.system_module as task_system_module,
                
                -- STATUS DE ENTREGA E FATURAMENTO
                CASE WHEN EXISTS(SELECT 1 FROM delivery d2 WHERE d2.task_id = t.id) THEN 'Sim' ELSE 'Não' END as has_delivery,
                CASE WHEN EXISTS(SELECT 1 FROM billing_period_task bpt WHERE bpt.task_id = t.id) THEN 'Sim' ELSE 'Não' END as has_quote_in_billing,
                
                -- DADOS REMOVIDOS (não há mais orçamentos)
                NULL as quote_id,
                NULL as quote_status,
                NULL as quote_amount,
                NULL as quote_created_at,
                
                -- DADOS DE ENTREGAS (LEFT JOIN com nova arquitetura)
                d.id as delivery_id,
                CASE d.status 
                    WHEN 'PENDING' THEN 'Pendente'
                    WHEN 'DEVELOPMENT' THEN 'Em Desenvolvimento' 
                    WHEN 'DELIVERED' THEN 'Entregue'
                    WHEN 'HOMOLOGATION' THEN 'Homologação'
                    WHEN 'APPROVED' THEN 'Aprovado'
                    WHEN 'REJECTED' THEN 'Rejeitado'
                    WHEN 'PRODUCTION' THEN 'Produção'
                    ELSE d.status
                END as delivery_status,
                p.name as project_name,
                di.pull_request as delivery_pull_request,
                di.branch as delivery_branch,
                di.notes as delivery_notes,
                di.started_at as delivery_started_at,
                di.finished_at as delivery_finished_at,
                
                -- DADOS DE FATURAMENTO
                bp.year as billing_year,
                bp.month as billing_month,
                bp.status as billing_status
                
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN delivery d ON d.task_id = t.id
            LEFT JOIN delivery_item di ON di.delivery_id = d.id
            LEFT JOIN project p ON di.project_id = p.id
            LEFT JOIN billing_period_task bpt ON bpt.task_id = t.id
            LEFT JOIN billing_period bp ON bpt.billing_period_id = bp.id
            ORDER BY t.id DESC, d.id ASC, di.id ASC, bp.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();

            // DADOS DA TAREFA (0-6) - datas removidas
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_description", row[3]);
            map.put("task_priority", row[4]);
            map.put("task_amount", row[5]);
            map.put("requester_name", row[6]);

            // METADADOS DA TAREFA (7-10)
            map.put("created_by_name", row[7]);
            map.put("updated_by_name", row[8]);
            map.put("task_server_origin", row[9]);
            map.put("task_system_module", row[10]);

            // STATUS DE ENTREGA E FATURAMENTO (11-12)
            map.put("has_delivery", row[11]);
            map.put("has_quote_in_billing", row[12]);

            // DADOS REMOVIDOS - ORÇAMENTO (13-16)
            map.put("quote_id", row[13]);
            map.put("quote_status", row[14]);
            map.put("quote_amount", row[15]);
            map.put("quote_created_at", row[16]);

            // DADOS DE ENTREGAS (17-25) - Pull Request reorganizado + notas + status traduzido
            map.put("delivery_id", row[17]);
            map.put("delivery_status", row[18]); // Status já traduzido na query
            map.put("project_name", row[19]);
            map.put("delivery_pull_request", row[20]); // Link da entrega
            map.put("delivery_branch", row[21]);
            map.put("delivery_notes", row[22]); // Nova coluna de notas
            map.put("delivery_started_at", row[23]);
            map.put("delivery_finished_at", row[24]);

            // DADOS DE FATURAMENTO (25-27) - No final
            map.put("billing_year", row[25]);
            map.put("billing_month", row[26]);
            map.put("billing_status", row[27]);

            return map;
        }).collect(Collectors.toList());

        log.debug("GENERAL REPORT generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateGeneralReport(data);
        log.debug("GENERAL REPORT completed successfully");

        return result;
    }

    @Override
    public byte[] exportGeneralReportForUser() throws IOException {
        log.debug("GENERAL REPORT FOR USER EXPORT STARTED");

        // Query sem dados de orçamento, faturamento e valor da tarefa
        String sql = """
            SELECT 
                -- DADOS DA TAREFA (sem valor, sem datas)
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.priority as task_priority,
                r.name as requester_name,
                
                -- METADADOS DA TAREFA
                cb.username as created_by_name,
                ub.username as updated_by_name,
                t.server_origin as task_server_origin,
                t.system_module as task_system_module,
                
                -- STATUS DE ENTREGA (sem faturamento para USER)
                CASE WHEN EXISTS(SELECT 1 FROM delivery d2 WHERE d2.task_id = t.id) THEN 'Sim' ELSE 'Não' END as has_delivery,
                
                -- DADOS DE ENTREGAS (nova arquitetura com status traduzido)
                d.id as delivery_id,
                CASE d.status 
                    WHEN 'PENDING' THEN 'Pendente'
                    WHEN 'DEVELOPMENT' THEN 'Em Desenvolvimento' 
                    WHEN 'DELIVERED' THEN 'Entregue'
                    WHEN 'HOMOLOGATION' THEN 'Homologação'
                    WHEN 'APPROVED' THEN 'Aprovado'
                    WHEN 'REJECTED' THEN 'Rejeitado'
                    WHEN 'PRODUCTION' THEN 'Produção'
                    ELSE d.status
                END as delivery_status,
                p.name as project_name,
                di.pull_request as delivery_pull_request,
                di.branch as delivery_branch,
                di.notes as delivery_notes,
                di.started_at as delivery_started_at,
                di.finished_at as delivery_finished_at
                
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN delivery d ON d.task_id = t.id
            LEFT JOIN delivery_item di ON di.delivery_id = d.id
            LEFT JOIN project p ON di.project_id = p.id
            ORDER BY t.id DESC, d.id ASC, di.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();

            // DADOS DA TAREFA (0-5) - sem valor, sem datas
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_description", row[3]);
            map.put("task_priority", row[4]);
            map.put("requester_name", row[5]);

            // METADADOS DA TAREFA (6-9)
            map.put("created_by_name", row[6]);
            map.put("updated_by_name", row[7]);
            map.put("task_server_origin", row[8]);
            map.put("task_system_module", row[9]);

            // STATUS DE ENTREGA (10) - sem faturamento para USER
            map.put("has_delivery", row[10]);

            // DADOS DE ENTREGAS (11-19) - status já traduzido
            map.put("delivery_id", row[11]);
            map.put("delivery_status", row[12]); // Status já traduzido na query
            map.put("project_name", row[13]);
            map.put("delivery_pull_request", row[14]);
            map.put("delivery_branch", row[15]);
            map.put("delivery_notes", row[16]); // Nova coluna de notas
            map.put("delivery_started_at", row[17]);
            map.put("delivery_finished_at", row[18]);

            return map;
        }).collect(Collectors.toList());

        log.debug("GENERAL REPORT FOR USER generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateGeneralReportForUser(data);
        log.debug("GENERAL REPORT FOR USER completed successfully");

        return result;
    }

    @Override
    public void sendFinancialEmail(Long taskId, List<String> additionalEmails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Inicializar relacionamentos lazy antes da chamada assíncrona
        task.getRequester().getName();

        try {
            emailService.sendFinancialNotificationAsync(task, additionalEmails);
            // Marca como enviado
            task.setFinancialEmailSent(true);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("Failed to send financial email notification for task {}: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Failed to send financial email notification");
        }
    }

    @Override
    @Transactional
    public void sendTaskEmail(Long taskId, List<String> additionalEmails) {

        final Task task = taskRepository.findById(taskId)
                .map(entity -> {
                    // Inicializar relacionamentos lazy antes do envio assíncrono
                    if (entity.getRequester() != null) {
                        entity.getRequester().getName();
                        entity.getRequester().getEmail();
                    }
                    if (entity.getCreatedBy() != null) {
                        entity.getCreatedBy().getUsername();
                        entity.getCreatedBy().getName();
                    }
                    if (entity.getUpdatedBy() != null) {
                        entity.getUpdatedBy().getUsername();
                        entity.getUpdatedBy().getName();
                    }
                    return entity;
                })
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + taskId));

        // Enviar email de notificação
        emailService.sendTaskUpdatedNotification(task, additionalEmails);

        // Marcar email como enviado
        task.setTaskEmailSent(true);
        taskRepository.save(task);
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        String generatedCode = code.toString();

        if (taskRepository.existsByCode(generatedCode)) {
            log.debug("Generated code {} already exists, generating new one", generatedCode);
            return generateRandomCode();
        }

        return generatedCode;
    }

    /**
     * Cria automaticamente uma entrega para a tarefa se ela não tiver uma
     */
    private void ensureTaskHasDelivery(Task task) {
        if (!deliveryService.existsByTaskId(task.getId())) {
            log.debug("Creating automatic delivery for task ID: {}", task.getId());
            
            DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                    .taskId(task.getId())
                    .status("PENDING")
                    .build();
            
            deliveryService.create(deliveryRequest);
            log.debug("Automatic delivery created for task ID: {}", task.getId());
        }
    }
}
