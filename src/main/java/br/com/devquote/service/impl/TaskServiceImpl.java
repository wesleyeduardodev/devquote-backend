package br.com.devquote.service.impl;
import br.com.devquote.adapter.BillingPeriodAdapter;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.adapter.TaskAdapter;
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
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final DeliveryService deliveryService;
    private final SecurityUtils securityUtils;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;
    private final EmailService emailService;

    @Override
    public List<TaskResponse> findAll() {
        List<TaskResponse> tasks = taskRepository.findAllOrderedById().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());

        if (!tasks.isEmpty()) {
            List<Long> taskIds = tasks.stream().map(TaskResponse::getId).toList();

            // Buscar informações de Billing
            Map<Long, Boolean> taskInBillingMap = getTaskInBillingStatus(taskIds);

            tasks.forEach(dto -> {
                List<SubTask> subTasks = subTaskRepository.findByTaskId(dto.getId());
                dto.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));

                // Adicionar informações de Billing
                dto.setHasQuote(false); // Não há mais quotes
                dto.setHasQuoteInBilling(taskInBillingMap.getOrDefault(dto.getId(), false));
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

        // Adicionar informações de Billing para o item específico
        response.setHasQuote(false); // Não há mais quotes
        response.setHasQuoteInBilling(billingPeriodTaskService.existsByTaskId(id));

        return response;
    }

    @Override
    public TaskResponse create(TaskRequest dto) {
        validateCreatePermission();

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Usuário não autenticado", "USER_NOT_AUTHENTICATED");
        }

        log.info("TASK CREATE requesterId={} title={} user={}",
            dto.getRequesterId(), dto.getTitle(), currentUser.getUsername());

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", dto.getRequesterId()));

        Task entity = TaskAdapter.toEntity(dto, requester);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);

        entity = taskRepository.save(entity);
        
        // Enviar notificação por email
        try {
            emailService.sendTaskCreatedNotification(entity);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task creation: {}", e.getMessage());
        }
        
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public TaskResponse update(Long id, TaskRequest dto) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(entity, "editar");

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        TaskAdapter.updateEntityFromDto(dto, entity, requester);
        entity.setUpdatedBy(currentUser);

        entity = taskRepository.save(entity);
        
        // Enviar notificação por email
        try {
            emailService.sendTaskUpdatedNotification(entity);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task update: {}", e.getMessage());
        }
        
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(entity, "excluir");

        User currentUser = securityUtils.getCurrentUser();
        log.warn("TASK DELETE id={} title={} user={}",
            id, entity.getTitle(), currentUser != null ? currentUser.getUsername() : "unknown");

        // Enviar notificação por email antes da exclusão
        try {
            emailService.sendTaskDeletedNotification(entity);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task deletion: {}", e.getMessage());
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

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Task task = buildAndPersistTask(dto, requester);

        List<SubTask> persistedSubTasks = persistSubTasks(task, dto.getSubTasks());

        // Processa o valor da tarefa conforme nova lógica
        processTaskAmount(task, persistedSubTasks);
        task = taskRepository.save(task);

        // Linkar task ao faturamento se solicitado
        if (isTrue(dto.getLinkTaskToBilling())) {
            LocalDate today = LocalDate.now();
            BillingPeriod billingMonth = getOrCreateBillingPeriod(today);
            ensureTaskLinkedToBillingMonth(task.getId(), billingMonth.getId());
        }

        createDeliveries(dto, task);

        // Enviar notificação por email
        try {
            emailService.sendTaskCreatedNotification(task);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task creation: {}", e.getMessage());
        }

        return buildTaskWithSubTasksResponse(task, persistedSubTasks);
    }

    private void createDeliveries(TaskWithSubTasksCreateRequest dto, Task task) {
        if (dto.getCreateDeliveries() != null && dto.getCreateDeliveries() && dto.getProjectsIds() != null && !dto.getProjectsIds().isEmpty()) {
            for (Long projectId : dto.getProjectsIds()) {
                DeliveryRequest deliveryRequest = DeliveryRequest
                        .builder()
                        .taskId(task.getId())
                        .projectId(projectId)
                        .status("PENDING")
                        .build();
                deliveryService.create(deliveryRequest);
            }
        }
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

        // Valida se pode alterar a flag hasSubTasks
        Boolean newHasSubTasks = dto.getHasSubTasks() != null ? dto.getHasSubTasks() : false;
        validateCanRemoveSubTasksFlag(task, newHasSubTasks);

        TaskAdapter.updateEntityFromDto(TaskRequest.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
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

        // Enviar notificação por email
        try {
            emailService.sendTaskUpdatedNotification(task);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task update with subtasks: {}", e.getMessage());
        }

        return buildTaskWithSubTasksResponse(task, updated);
    }

    @Override
    public void deleteTaskWithSubTasks(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateTaskAccess(task, "excluir");

        if (billingPeriodTaskService.existsByTaskId(taskId)) {
            throw new RuntimeException("Cannot delete task. It is linked to a billing period.");
        }
        
        // Deletar entregas vinculadas à task
        deliveryService.deleteByTaskId(taskId);

        // Enviar notificação por email antes da exclusão
        try {
            emailService.sendTaskDeletedNotification(task);
        } catch (Exception e) {
            log.warn("Failed to send email notification for task with subtasks deletion: {}", e.getMessage());
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
                                               String status,
                                               String code,
                                               String link,
                                               String createdAt,
                                               String updatedAt,
                                               Pageable pageable) {

        // Todos os usuários podem ver todas as tarefas
        Page<Task> page = taskRepository.findByOptionalFieldsPaginated(
                id, requesterId, requesterName, title, description, status, code, link, createdAt, updatedAt, pageable
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

        // Buscar informações de Billing
        Map<Long, Boolean> taskInBillingMap = getTaskInBillingStatus(taskIds);

        dtos.forEach(dto -> {
            List<SubTask> list = subTasksByTaskId.getOrDefault(dto.getId(), List.of());
            dto.setSubTasks(SubTaskAdapter.toResponseDTOList(list));

            // Adicionar informações de Billing
            dto.setHasQuote(false); // Não há mais quotes
            dto.setHasQuoteInBilling(taskInBillingMap.getOrDefault(dto.getId(), false));
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
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
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
                .status("PENDING")
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
                .status(task.getStatus())
                .code(task.getCode())
                .link(task.getLink())
                .meetingLink(task.getMeetingLink())
                .notes(task.getNotes())
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

    @Override
    public byte[] exportTasksToExcel() throws IOException {
        log.info("EXCEL EXPORT STARTED");

        // Verificar perfil do usuário para controle de colunas de valor
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("Usuário não autenticado", "USER_NOT_AUTHENTICATED");
        }

        var profiles = currentUser.getActiveProfileCodes();
        boolean canViewAmounts = profiles.contains("ADMIN") || profiles.contains("MANAGER");
        log.info("EXCEL EXPORT user={} canViewAmounts={}", currentUser.getUsername(), canViewAmounts);

        // Consulta nativa SQL para obter todos os dados de tarefas e subtarefas
        String sql = """
            SELECT 
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.status as task_status,
                t.task_type as task_type,
                t.priority as task_priority,
                r.name as requester_name,
                cb.username as created_by_user,
                ub.username as updated_by_user,
                t.server_origin,
                t.system_module,
                t.link as task_link,
                t.meeting_link,
                t.notes as task_notes,
                t.amount as task_amount,
                t.has_sub_tasks as has_subtasks,
                'Não' as has_quote,
                CASE WHEN tbmt.id IS NOT NULL THEN 'Sim' ELSE 'Não' END as has_quote_in_billing,
                t.created_at as task_created_at,
                t.updated_at as task_updated_at,
                st.id as subtask_id,
                st.title as subtask_title,
                st.description as subtask_description,
                st.status as subtask_status,
                st.amount as subtask_amount
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN task_billing_month_task tbmt ON tbmt.task_id = t.id
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
            map.put("task_status", row[4]);
            map.put("task_type", row[5]);
            map.put("task_priority", row[6]);
            map.put("requester_name", row[7]);
            map.put("created_by_user", row[8]);
            map.put("updated_by_user", row[9]);
            map.put("server_origin", row[10]);
            map.put("system_module", row[11]);
            map.put("task_link", row[12]);
            map.put("meeting_link", row[13]);
            map.put("task_notes", row[14]);
            map.put("task_amount", row[15]);
            map.put("has_subtasks", Boolean.TRUE.equals(row[16]) ? "Sim" : "Não");
            map.put("has_quote", row[17]);
            map.put("has_quote_in_billing", row[18]);
            map.put("task_created_at", row[19]);
            map.put("task_updated_at", row[20]);
            map.put("subtask_id", row[21]);
            map.put("subtask_title", row[22]);
            map.put("subtask_description", row[23]);
            map.put("subtask_status", row[24]);
            map.put("subtask_amount", row[25]);
            return map;
        }).collect(Collectors.toList());

        log.info("EXCEL EXPORT generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateTasksReport(data, canViewAmounts);
        log.info("EXCEL EXPORT completed successfully");

        return result;
    }

    @Override
    public byte[] exportGeneralReport() throws IOException {
        log.info("GENERAL REPORT EXPORT STARTED");

        // Query incluindo subtarefas na estrutura correta: Tarefas → Subtarefas → Orçamentos → Entregas → Faturamento
        String sql = """
            SELECT 
                -- DADOS DA TAREFA
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.status as task_status,
                t.priority as task_priority,
                t.amount as task_amount,
                r.name as requester_name,
                t.created_at as task_created_at,
                t.updated_at as task_updated_at,
                
                -- METADADOS DA TAREFA
                cb.username as created_by_name,
                ub.username as updated_by_name,
                t.server_origin as task_server_origin,
                t.system_module as task_system_module,
                
                -- DADOS REMOVIDOS (não há mais orçamentos)
                NULL as quote_id,
                NULL as quote_status,
                NULL as quote_amount,
                NULL as quote_created_at,
                
                -- DADOS DE ENTREGAS (LEFT JOIN direto para múltiplas linhas)
                d.id as delivery_id,
                d.status as delivery_status,
                p.name as project_name,
                d.pull_request as delivery_pull_request,
                d.branch as delivery_branch,
                d.script as delivery_script,
                d.notes as delivery_notes,
                d.started_at as delivery_started_at,
                d.finished_at as delivery_finished_at,
                
                -- DADOS DE FATURAMENTO
                tbm.year as billing_year,
                tbm.month as billing_month,
                tbm.status as billing_status
                
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN delivery d ON d.task_id = t.id
            LEFT JOIN project p ON d.project_id = p.id
            LEFT JOIN task_billing_month_task tbmt ON tbmt.task_id = t.id
            LEFT JOIN task_billing_month tbm ON tbmt.task_billing_month_id = tbm.id
            ORDER BY t.id DESC, d.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();

            // DADOS DA TAREFA (0-9)
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_description", row[3]);
            map.put("task_status", row[4]);
            map.put("task_priority", row[5]);
            map.put("task_amount", row[6]);
            map.put("requester_name", row[7]);
            map.put("task_created_at", row[8]);
            map.put("task_updated_at", row[9]);

            // METADADOS DA TAREFA (10-13)
            map.put("created_by_name", row[10]);
            map.put("updated_by_name", row[11]);
            map.put("task_server_origin", row[12]);
            map.put("task_system_module", row[13]);

            // DADOS DE ORÇAMENTO (14-17)
            map.put("quote_id", row[14]);
            map.put("quote_status", row[15]);
            map.put("quote_amount", row[16]);
            map.put("quote_created_at", row[17]);

            // DADOS DE ENTREGAS (18-26) - Pull Request reorganizado + notas
            map.put("delivery_id", row[18]);
            map.put("delivery_status", row[19]);
            map.put("project_name", row[20]);
            map.put("delivery_pull_request", row[21]); // Link da entrega
            map.put("delivery_branch", row[22]);
            map.put("delivery_script", row[23]);
            map.put("delivery_notes", row[24]); // Nova coluna de notas
            map.put("delivery_started_at", row[25]);
            map.put("delivery_finished_at", row[26]);

            // DADOS DE FATURAMENTO (27-29) - No final
            map.put("billing_year", row[27]);
            map.put("billing_month", row[28]);
            map.put("billing_status", row[29]);

            return map;
        }).collect(Collectors.toList());

        log.info("GENERAL REPORT generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateGeneralReport(data);
        log.info("GENERAL REPORT completed successfully");

        return result;
    }

    @Override
    public byte[] exportGeneralReportForUser() throws IOException {
        log.info("GENERAL REPORT FOR USER EXPORT STARTED");

        // Query sem dados de orçamento, faturamento e valor da tarefa
        String sql = """
            SELECT 
                -- DADOS DA TAREFA (sem valor)
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.description as task_description,
                t.status as task_status,
                t.priority as task_priority,
                r.name as requester_name,
                t.created_at as task_created_at,
                t.updated_at as task_updated_at,
                
                -- METADADOS DA TAREFA
                cb.username as created_by_name,
                ub.username as updated_by_name,
                t.server_origin as task_server_origin,
                t.system_module as task_system_module,
                
                -- DADOS DE ENTREGAS
                d.id as delivery_id,
                d.status as delivery_status,
                p.name as project_name,
                d.pull_request as delivery_pull_request,
                d.branch as delivery_branch,
                d.script as delivery_script,
                d.notes as delivery_notes,
                d.started_at as delivery_started_at,
                d.finished_at as delivery_finished_at
                
            FROM task t
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN users cb ON t.created_by = cb.id
            LEFT JOIN users ub ON t.updated_by = ub.id
            LEFT JOIN delivery d ON d.task_id = t.id
            LEFT JOIN project p ON d.project_id = p.id
            ORDER BY t.id DESC, d.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();

            // DADOS DA TAREFA (0-8) - sem valor
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_description", row[3]);
            map.put("task_status", row[4]);
            map.put("task_priority", row[5]);
            map.put("requester_name", row[6]);
            map.put("task_created_at", row[7]);
            map.put("task_updated_at", row[8]);

            // METADADOS DA TAREFA (9-12)
            map.put("created_by_name", row[9]);
            map.put("updated_by_name", row[10]);
            map.put("task_server_origin", row[11]);
            map.put("task_system_module", row[12]);

            // DADOS DE ENTREGAS (13-21)
            map.put("delivery_id", row[13]);
            map.put("delivery_status", row[14]);
            map.put("project_name", row[15]);
            map.put("delivery_pull_request", row[16]);
            map.put("delivery_branch", row[17]);
            map.put("delivery_script", row[18]);
            map.put("delivery_notes", row[19]); // Nova coluna de notas
            map.put("delivery_started_at", row[20]);
            map.put("delivery_finished_at", row[21]);

            return map;
        }).collect(Collectors.toList());

        log.info("GENERAL REPORT FOR USER generating file with {} records", data.size());
        byte[] result = excelReportUtils.generateGeneralReportForUser(data);
        log.info("GENERAL REPORT FOR USER completed successfully");

        return result;
    }
}
