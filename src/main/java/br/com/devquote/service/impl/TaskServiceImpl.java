package br.com.devquote.service.impl;
import br.com.devquote.adapter.QuoteAdapter;
import br.com.devquote.adapter.QuoteBillingMonthAdapter;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.adapter.TaskAdapter;
import br.com.devquote.configuration.BillingProperties;
import br.com.devquote.dto.request.*;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.dto.response.QuoteResponse;
import br.com.devquote.dto.response.TaskResponse;
import br.com.devquote.dto.response.TaskWithSubTasksResponse;
import br.com.devquote.entity.*;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.repository.RequesterRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final RequesterRepository requesterRepository;
    private final SubTaskRepository subTaskRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteService quoteService;
    private final QuoteBillingMonthService quoteBillingMonthService;
    private final QuoteBillingMonthQuoteService quoteBillingMonthQuoteService;
    private final BillingProperties billingProperties;
    private final DeliveryService deliveryService;

    @Override
    public List<TaskResponse> findAll() {
        List<TaskResponse> tasks = taskRepository.findAllOrderedById().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());

        tasks.forEach(dto -> {
            List<SubTask> subTasks = subTaskRepository.findByTaskId(dto.getId());
            dto.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));
        });

        return tasks;
    }

    @Override
    public TaskResponse findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskResponse response = TaskAdapter.toResponseDTO(task);
        List<SubTask> subTasks = subTaskRepository.findByTaskId(response.getId());
        response.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));
        return response;
    }

    @Override
    public TaskResponse create(TaskRequest dto) {
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        Task entity = taskRepository.save(TaskAdapter.toEntity(dto, requester));
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public TaskResponse update(Long id, TaskRequest dto) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        TaskAdapter.updateEntityFromDto(dto, entity, requester);
        entity = taskRepository.save(entity);
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            deleteTaskWithSubTasks(id);
        }
    }

    @Override
    public TaskWithSubTasksResponse createWithSubTasks(TaskWithSubTasksCreateRequest dto) {

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Task task = buildAndPersistTask(dto, requester);

        List<SubTask> persistedSubTasks = persistSubTasks(task, dto.getSubTasks());

        QuoteResponse quoteResponse = ensureQuoteIfRequested(task, dto);

        if (isTrue(dto.getLinkQuoteToBilling()) && quoteResponse != null) {
            LocalDate today = LocalDate.now();
            QuoteBillingMonth billingMonth = getOrCreateBillingMonth(today);
            ensureQuoteLinkedToBillingMonth(quoteResponse.getId(), billingMonth.getId());
        }

        createDeliveries(dto, quoteResponse);

        return buildTaskWithSubTasksResponse(task, persistedSubTasks);
    }

    private void createDeliveries(TaskWithSubTasksCreateRequest dto, QuoteResponse quoteResponse) {
        if (quoteResponse != null && dto.getCreateQuote() != null && dto.getCreateQuote() && dto.getProjectsIds() != null && !dto.getProjectsIds().isEmpty()) {
            for (Long projectId : dto.getProjectsIds()) {
                DeliveryRequest deliveryRequest = DeliveryRequest
                        .builder()
                        .quoteId(quoteResponse.getId())
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

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        TaskAdapter.updateEntityFromDto(TaskRequest.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
                .build(), task, requester);

        task = taskRepository.save(task);

        List<SubTask> updated = upsertAndDeleteSubTasks(task, dto.getSubTasks());

        return buildTaskWithSubTasksResponse(task, updated);
    }

    @Override
    public void deleteTaskWithSubTasks(Long taskId) {
        if (quoteRepository.existsByTaskId(taskId)) {
            throw new RuntimeException("Cannot delete task. It is linked to a quote.");
        }
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found");
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

        Page<Task> page = taskRepository.findByOptionalFieldsPaginated(
                id, requesterId, requesterName, title, description, status, code, link, createdAt, updatedAt, pageable
        );

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

        dtos.forEach(dto -> {
            List<SubTask> list = subTasksByTaskId.getOrDefault(dto.getId(), List.of());
            dto.setSubTasks(SubTaskAdapter.toResponseDTOList(list));
        });

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private Task buildAndPersistTask(TaskWithSubTasksCreateRequest dto, Requester requester) {
        Task task = TaskAdapter.toEntity(TaskRequest.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
                .build(), requester);
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


    private QuoteResponse ensureQuoteIfRequested(Task task, TaskWithSubTasksCreateRequest dto) {
        if (isTrue(dto.getCreateQuote())) {
            QuoteRequest req = QuoteRequest.builder()
                    .taskId(task.getId())
                    .status("PENDING")
                    .totalAmount(dto.getTotalAmount())
                    .build();
            return quoteService.create(req);
        }

        Quote existing = quoteService.findByTaskId(task.getId());
        return existing != null ? QuoteAdapter.toResponseDTO(existing) : null;
    }

    private QuoteBillingMonth getOrCreateBillingMonth(LocalDate baseDate) {
        int year = baseDate.getYear();
        int month = baseDate.getMonthValue();

        QuoteBillingMonth found = quoteBillingMonthService.findByYearAndMonth(year, month);
        if (found != null) return found;

        LocalDate paymentDate = computeNextMonthPaymentDate(baseDate, billingProperties.getPaymentDay());

        QuoteBillingMonthRequest createDto = QuoteBillingMonthRequest.builder()
                .year(year)
                .month(month)
                .paymentDate(paymentDate)
                .status("PENDING")
                .build();

        QuoteBillingMonthResponse created = quoteBillingMonthService.create(createDto);
        return QuoteBillingMonthAdapter.toEntity(created);
    }

    private LocalDate computeNextMonthPaymentDate(LocalDate base, int desiredDay) {
        LocalDate nextMonthFirst = base.plusMonths(1).withDayOfMonth(1);
        int day = Math.min(Math.max(desiredDay, 1), nextMonthFirst.lengthOfMonth());
        return nextMonthFirst.withDayOfMonth(day);
    }

    private void ensureQuoteLinkedToBillingMonth(Long quoteId, Long billingMonthId) {
        QuoteBillingMonthQuote existing =
                quoteBillingMonthQuoteService.findByQuoteBillingMonthIdAndQuoteId(billingMonthId, quoteId);
        if (existing != null) return;

        QuoteBillingMonthQuoteRequest linkReq = QuoteBillingMonthQuoteRequest.builder()
                .quoteId(quoteId)
                .quoteBillingMonthId(billingMonthId)
                .build();

        quoteBillingMonthQuoteService.create(linkReq);
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
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
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
}
