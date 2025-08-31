package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAdapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.DeliveryService;
import br.com.devquote.service.EmailService;
import br.com.devquote.utils.ExcelReportUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;
    private final EmailService emailService;

    @Override
    public List<DeliveryResponse> findAll() {
        return deliveryRepository.findAllOrderedById().stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryResponse findById(Long id) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (entity.getTask() != null) {
            entity.getTask().getId();
        }
        if (entity.getProject() != null) {
            entity.getProject().getId();
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse create(DeliveryRequest dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Delivery entity = DeliveryAdapter.toEntity(dto, task, project);
        entity = deliveryRepository.save(entity);

        // Enviar notificação por email
        try {
            // Fazer fetch explícito das entidades relacionadas antes do método assíncrono
            Delivery deliveryWithRelations = deliveryRepository.findById(entity.getId())
                .map(d -> {
                    // Inicializar relacionamentos lazy
                    if (d.getTask() != null) {
                        d.getTask().getTitle(); // Inicializa Task
                        if (d.getTask().getRequester() != null) {
                            d.getTask().getRequester().getName(); // Inicializa Requester
                            d.getTask().getRequester().getEmail();
                        }
                    }
                    if (d.getProject() != null) {
                        d.getProject().getName(); // Inicializa Project
                    }
                    if (d.getCreatedBy() != null) {
                        d.getCreatedBy().getUsername(); // Inicializa User
                    }
                    return d;
                })
                .orElse(entity);

            emailService.sendDeliveryCreatedNotification(deliveryWithRelations);
        } catch (Exception e) {
            log.warn("Failed to send email notification for delivery creation: {}", e.getMessage());
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse update(Long id, DeliveryRequest dto) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        DeliveryAdapter.updateEntityFromDto(dto, entity, task, project);
        entity = deliveryRepository.save(entity);

        // Enviar notificação por email
        try {
            // Fazer fetch explícito das entidades relacionadas antes do método assíncrono
            Delivery deliveryWithRelations = deliveryRepository.findById(entity.getId())
                .map(d -> {
                    // Inicializar relacionamentos lazy
                    if (d.getTask() != null) {
                        d.getTask().getTitle(); // Inicializa Task
                        if (d.getTask().getRequester() != null) {
                            d.getTask().getRequester().getName(); // Inicializa Requester
                            d.getTask().getRequester().getEmail();
                        }
                    }
                    if (d.getProject() != null) {
                        d.getProject().getName(); // Inicializa Project
                    }
                    if (d.getCreatedBy() != null) {
                        d.getCreatedBy().getUsername(); // Inicializa User
                    }
                    return d;
                })
                .orElse(entity);

            emailService.sendDeliveryUpdatedNotification(deliveryWithRelations);
        } catch (Exception e) {
            log.warn("Failed to send email notification for delivery update: {}", e.getMessage());
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        // Enviar notificação por email antes da exclusão
        try {
            log.info("Initiating email notification for delivery deletion with ID: {}", id);
            
            // Fazer fetch explícito das entidades relacionadas antes do método assíncrono
            Delivery deliveryWithRelations = deliveryRepository.findById(id)
                .map(d -> {
                    log.debug("Initializing lazy relationships for delivery ID: {}", d.getId());
                    // Inicializar relacionamentos lazy
                    if (d.getTask() != null) {
                        log.debug("Initializing Task relationship");
                        d.getTask().getTitle(); // Inicializa Task
                        log.debug("Task initialized: {}", d.getTask().getTitle());
                        if (d.getTask().getRequester() != null) {
                            d.getTask().getRequester().getName(); // Inicializa Requester
                            d.getTask().getRequester().getEmail();
                            log.debug("Requester initialized: {}", d.getTask().getRequester().getName());
                        }
                    }
                    if (d.getProject() != null) {
                        d.getProject().getName(); // Inicializa Project
                        log.debug("Project initialized: {}", d.getProject().getName());
                    }
                    if (d.getCreatedBy() != null) {
                        d.getCreatedBy().getUsername(); // Inicializa User
                        log.debug("CreatedBy initialized: {}", d.getCreatedBy().getUsername());
                    }
                    return d;
                })
                .orElse(entity);

            log.info("Calling emailService.sendDeliveryDeletedNotification for delivery ID: {}", id);
            emailService.sendDeliveryDeletedNotification(deliveryWithRelations);
            log.info("Email service call completed for delivery deletion ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to send email notification for delivery deletion ID: {}", id, e);
        }

        deliveryRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        deliveryRepository.deleteAllById(ids);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        if (taskId == null) {
            return;
        }
        
        log.info("Starting deletion of deliveries for task ID: {}", taskId);
        
        // Buscar todas as deliveries da task antes de deletar
        List<Delivery> deliveriesToDelete = deliveryRepository.findByTaskId(taskId);
        
        if (deliveriesToDelete.isEmpty()) {
            log.info("No deliveries found for task ID: {}", taskId);
            return;
        }
        
        log.info("Found {} deliveries to delete for task ID: {}", deliveriesToDelete.size(), taskId);
        
        // Enviar notificação por email para cada delivery antes da exclusão
        for (Delivery delivery : deliveriesToDelete) {
            try {
                log.info("Sending deletion notification for delivery ID: {}", delivery.getId());
                
                // Fazer fetch explícito das entidades relacionadas antes do método assíncrono
                Delivery deliveryWithRelations = deliveryRepository.findById(delivery.getId())
                    .map(d -> {
                        log.debug("Initializing lazy relationships for delivery ID: {}", d.getId());
                        // Inicializar relacionamentos lazy
                        if (d.getTask() != null) {
                            log.debug("Initializing Task relationship");
                            d.getTask().getTitle(); // Inicializa Task
                            log.debug("Task initialized: {}", d.getTask().getTitle());
                            if (d.getTask().getRequester() != null) {
                                d.getTask().getRequester().getName(); // Inicializa Requester
                                d.getTask().getRequester().getEmail();
                                log.debug("Requester initialized: {}", d.getTask().getRequester().getName());
                            }
                        }
                        if (d.getProject() != null) {
                            d.getProject().getName(); // Inicializa Project
                            log.debug("Project initialized: {}", d.getProject().getName());
                        }
                        if (d.getCreatedBy() != null) {
                            d.getCreatedBy().getUsername(); // Inicializa User
                            log.debug("CreatedBy initialized: {}", d.getCreatedBy().getUsername());
                        }
                        return d;
                    })
                    .orElse(delivery);

                log.info("Calling emailService.sendDeliveryDeletedNotification for delivery ID: {}", delivery.getId());
                emailService.sendDeliveryDeletedNotification(deliveryWithRelations);
                log.info("Email notification sent for delivery ID: {}", delivery.getId());
                
            } catch (Exception e) {
                log.error("Failed to send email notification for delivery deletion ID: {}", delivery.getId(), e);
            }
        }
        
        // Deletar todas as deliveries da task
        deliveryRepository.deleteByTaskId(taskId);
        log.info("Successfully deleted {} deliveries for task ID: {}", deliveriesToDelete.size(), taskId);
    }

    @Override
    public Page<DeliveryResponse> findAllPaginated(Long id,
                                                   String taskName,
                                                   String taskCode,
                                                   String projectName,
                                                   String branch,
                                                   String pullRequest,
                                                   String status,
                                                   String startedAt,
                                                   String finishedAt,
                                                   String createdAt,
                                                   String updatedAt,
                                                   Pageable pageable) {

        Page<Delivery> page = deliveryRepository.findByOptionalFieldsPaginated(
                id, taskName, taskCode, projectName,
                branch, pullRequest, status, startedAt, finishedAt, createdAt, updatedAt, pageable
        );

        return page.map(DeliveryAdapter::toResponseDTO);
    }

    /**
     * Calcula o status geral das entregas baseado nos status individuais
     */
    private String calculateDeliveryStatus(List<Delivery> deliveries) {
        if (deliveries.isEmpty()) {
            return "PENDING";
        }
        
        // Considera DELIVERED e APPROVED como entregas finalizadas
        long completedCount = deliveries.stream()
                .filter(d -> "DELIVERED".equals(d.getStatus()) || "APPROVED".equals(d.getStatus()))
                .count();
        
        if (completedCount == deliveries.size()) {
            return "COMPLETED"; // Todas entregues/aprovadas
        } else if (completedCount > 0) {
            return "IN_PROGRESS"; // Algumas entregues, outras não
        } else {
            return "PENDING"; // Nenhuma entregue
        }
    }

    @Override
    public Page<DeliveryGroupResponse> findAllGroupedByTask(String taskName,
                                                             String taskCode,
                                                             String status,
                                                             String createdAt,
                                                             String updatedAt,
                                                             Pageable pageable) {

        // Buscar todas as entregas com os filtros
        List<Delivery> allDeliveries = deliveryRepository.findByTaskFilters(
                taskName, taskCode, status, createdAt, updatedAt
        );

        // Agrupar por Task ID
        Map<Long, List<Delivery>> groupedByTask = allDeliveries.stream()
                .collect(Collectors.groupingBy(delivery -> delivery.getTask().getId()));

        // Converter para DeliveryGroupResponse
        List<DeliveryGroupResponse> groupedDeliveries = groupedByTask.entrySet().stream()
                .map(entry -> {
                    Long taskId = entry.getKey();
                    List<Delivery> deliveries = entry.getValue();
                    Delivery firstDelivery = deliveries.get(0);
                    Task task = firstDelivery.getTask();

                    List<DeliveryResponse> deliveryResponses = deliveries.stream()
                            .map(DeliveryAdapter::toResponseDTO)
                            .collect(Collectors.toList());

                    long completedCount = deliveries.stream()
                            .filter(d -> "DELIVERED".equals(d.getStatus()) || "APPROVED".equals(d.getStatus()))
                            .count();

                    long pendingCount = deliveries.stream()
                            .filter(d -> !"DELIVERED".equals(d.getStatus()) && !"APPROVED".equals(d.getStatus()))
                            .count();

                    return DeliveryGroupResponse.builder()
                            .taskId(taskId)
                            .taskName(task.getTitle())
                            .taskCode(task.getCode())
                            .taskStatus(task.getStatus())
                            .deliveryStatus(calculateDeliveryStatus(deliveries))
                            .taskValue(task.getAmount())
                            .createdAt(task.getCreatedAt())
                            .updatedAt(task.getUpdatedAt())
                            .totalDeliveries(deliveries.size())
                            .completedDeliveries((int) completedCount)
                            .pendingDeliveries((int) pendingCount)
                            .deliveries(deliveryResponses)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getTaskId(), a.getTaskId())) // Ordenar por taskId decrescente
                .collect(Collectors.toList());

        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), groupedDeliveries.size());

        List<DeliveryGroupResponse> pageContent = start >= groupedDeliveries.size() ?
                List.of() : groupedDeliveries.subList(start, end);

        return new PageImpl<>(pageContent, pageable, groupedDeliveries.size());
    }

    @Override
    public DeliveryGroupResponse findGroupDetailsByTaskId(Long taskId) {
        List<Delivery> deliveries = deliveryRepository.findByTaskId(taskId);

        if (deliveries.isEmpty()) {
            throw new RuntimeException("No deliveries found for task ID: " + taskId);
        }

        Delivery firstDelivery = deliveries.get(0);
        Task task = firstDelivery.getTask();

        List<DeliveryResponse> deliveryResponses = deliveries.stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());

        long completedCount = deliveries.stream()
                .filter(d -> "DELIVERED".equals(d.getStatus()) || "APPROVED".equals(d.getStatus()))
                .count();

        long pendingCount = deliveries.stream()
                .filter(d -> !"DELIVERED".equals(d.getStatus()) && !"APPROVED".equals(d.getStatus()))
                .count();

        return DeliveryGroupResponse.builder()
                .taskId(taskId)
                .taskName(task.getTitle())
                .taskCode(task.getCode())
                .taskStatus(task.getStatus())
                .deliveryStatus(calculateDeliveryStatus(deliveries))
                .taskValue(task.getAmount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .totalDeliveries(deliveries.size())
                .completedDeliveries((int) completedCount)
                .pendingDeliveries((int) pendingCount)
                .deliveries(deliveryResponses)
                .build();
    }

    @Override
    public byte[] exportToExcel() throws IOException {
        String sql = """
            SELECT 
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.status as task_status,
                t.amount as task_amount,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                d.id as delivery_id,
                d.status as delivery_status,
                p.name as project_name,
                d.pull_request,
                d.branch,
                d.script,
                d.notes,
                d.started_at,
                d.finished_at,
                d.created_at as delivery_created_at,
                d.updated_at as delivery_updated_at
            FROM delivery d
            INNER JOIN project p ON d.project_id = p.id
            INNER JOIN task t ON d.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            ORDER BY d.id DESC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            // Dados da tarefa primeiro
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_status", row[3]);
            map.put("task_amount", row[4]);
            map.put("subtasks_count", row[5]);
            map.put("requester_name", row[6]);
            // Dados da entrega depois
            map.put("delivery_id", row[7]);
            map.put("delivery_status", row[8]);
            map.put("project_name", row[9]);
            map.put("pull_request", row[10]);
            map.put("branch", row[11]);
            map.put("script", row[12]);
            map.put("notes", row[13]);
            map.put("started_at", row[14]);
            map.put("finished_at", row[15]);
            map.put("delivery_created_at", row[16]);
            map.put("delivery_updated_at", row[17]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateDeliveriesReport(data);
    }
}
