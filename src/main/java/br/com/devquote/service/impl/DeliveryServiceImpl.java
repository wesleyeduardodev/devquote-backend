package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAdapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.DeliveryStatusCount;
import br.com.devquote.entity.*;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.enums.Environment;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.DeliveryService;
import br.com.devquote.service.DeliveryAttachmentService;
import br.com.devquote.service.DeliveryItemAttachmentService;
import br.com.devquote.service.DeliveryOperationalAttachmentService;
import br.com.devquote.service.EmailService;
import br.com.devquote.service.storage.FileStorageStrategy;
import br.com.devquote.utils.ExcelReportUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
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
    private final DeliveryAttachmentService deliveryAttachmentService;
    private final DeliveryItemAttachmentService deliveryItemAttachmentService;
    private final DeliveryOperationalAttachmentService deliveryOperationalAttachmentService;
    private final FileStorageStrategy fileStorageStrategy;

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

        if (entity.getItems() != null) {
            entity.getItems().size();
        }

        if (entity.getOperationalItems() != null) {
            entity.getOperationalItems().size();
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse create(DeliveryRequest dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (deliveryRepository.existsByTaskId(dto.getTaskId())) {
            throw new RuntimeException("Delivery already exists for this task");
        }

        Delivery entity = Delivery.builder()
                .task(task)
                .flowType(task.getFlowType())
                .environment(dto.getEnvironment() != null ? Environment.fromString(dto.getEnvironment()) : task.getEnvironment())
                .status(dto.getStatus() != null ? DeliveryStatus.fromString(dto.getStatus()) : DeliveryStatus.PENDING)
                .notes(dto.getNotes())
                .build();

        entity = deliveryRepository.save(entity);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            final Delivery savedDelivery = entity;
            dto.getItems().forEach(itemDto -> {
                Project project = projectRepository.findById(itemDto.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found with ID: " + itemDto.getProjectId()));
                
                var item = DeliveryItem.builder()
                        .delivery(savedDelivery)
                        .project(project)
                        .status(itemDto.getStatus() != null ? DeliveryStatus.fromString(itemDto.getStatus()) : DeliveryStatus.PENDING)
                        .branch(itemDto.getBranch())
                        .sourceBranch(itemDto.getSourceBranch())
                        .pullRequest(itemDto.getPullRequest())
                        .notes(itemDto.getNotes())
                        .startedAt(itemDto.getStartedAt())
                        .finishedAt(itemDto.getFinishedAt())
                        .build();
                
                savedDelivery.addItem(item);
            });

            entity.updateDates();
            entity = deliveryRepository.save(entity);
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse update(Long id, DeliveryRequest dto) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        entity.setTask(task);
        if (dto.getStatus() != null) {
            entity.setStatus(DeliveryStatus.fromString(dto.getStatus()));
        }
        if (dto.getEnvironment() != null) {
            entity.setEnvironment(Environment.fromString(dto.getEnvironment()));
        }
        entity.setNotes(dto.getNotes());

        entity.getItems().clear();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            final Delivery deliveryEntity = entity;
            dto.getItems().forEach(itemDto -> {
                Project project = projectRepository.findById(itemDto.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found with ID: " + itemDto.getProjectId()));
                
                var item = DeliveryItem.builder()
                        .delivery(deliveryEntity)
                        .project(project)
                        .status(itemDto.getStatus() != null ? DeliveryStatus.fromString(itemDto.getStatus()) : DeliveryStatus.PENDING)
                        .branch(itemDto.getBranch())
                        .sourceBranch(itemDto.getSourceBranch())
                        .pullRequest(itemDto.getPullRequest())
                        .notes(itemDto.getNotes())
                        .startedAt(itemDto.getStartedAt())
                        .finishedAt(itemDto.getFinishedAt())
                        .build();
                
                deliveryEntity.addItem(item);
            });
        }

        entity.updateDates();
        entity = deliveryRepository.save(entity);

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        deliveryRepository.findById(id)
                .map(d -> {
                    if (d.getTask() != null) {
                        d.getTask().getTitle();
                        if (d.getTask().getRequester() != null) {
                            d.getTask().getRequester().getName();
                            d.getTask().getRequester().getEmail();
                        }
                    }
                    if (d.getItems() != null) {
                        d.getItems().size();
                        d.getItems().forEach(item -> {
                            if (item.getProject() != null) {
                                item.getProject().getName();
                            }
                        });
                    }
                    return d;
                })
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Map<String, byte[]> attachmentDataMap = new HashMap<>();

        try {
            List<DeliveryAttachment> deliveryAttachments = deliveryAttachmentService.getDeliveryAttachmentsEntities(id);
            if (deliveryAttachments != null && !deliveryAttachments.isEmpty()) {
                for (DeliveryAttachment attachment : deliveryAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("delivery_" + attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download delivery attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }

            List<DeliveryItemAttachment> itemAttachments = deliveryItemAttachmentService.getDeliveryItemAttachmentsEntitiesByDeliveryId(id);
            if (itemAttachments != null && !itemAttachments.isEmpty()) {
                for (DeliveryItemAttachment attachment : itemAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("item_" + attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download item attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }

            List<DeliveryOperationalAttachment> operationalAttachments = deliveryOperationalAttachmentService.getOperationalAttachmentsEntitiesByDeliveryId(id);
            if (operationalAttachments != null && !operationalAttachments.isEmpty()) {
                for (DeliveryOperationalAttachment attachment : operationalAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("operational_" + attachment.getOriginalName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download operational attachment {} from S3: {}", attachment.getOriginalName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing attachments from database: {}", e.getMessage());
        }

        try {
            deliveryAttachmentService.deleteAllDeliveryAttachmentsAndFolder(id);
            deliveryItemAttachmentService.deleteAllDeliveryItemAttachmentsByDeliveryId(id);
            deliveryOperationalAttachmentService.deleteAllOperationalAttachmentsByDeliveryId(id);
        } catch (Exception e) {
            log.error("Failed to delete attachments for delivery {}: {}", id, e.getMessage());
        }

        deliveryRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long deliveryId : ids) {
            try {
                deliveryAttachmentService.deleteAllDeliveryAttachmentsAndFolder(deliveryId);
                deliveryItemAttachmentService.deleteAllDeliveryItemAttachmentsByDeliveryId(deliveryId);
                deliveryOperationalAttachmentService.deleteAllOperationalAttachmentsByDeliveryId(deliveryId);
                log.info("Successfully deleted all attachments for delivery {}", deliveryId);
            } catch (Exception e) {
                log.warn("Failed to delete attachments for delivery {}: {}", deliveryId, e.getMessage());
            }
        }

        deliveryRepository.deleteAllById(ids);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        if (taskId == null) {
            return;
        }

        log.debug("Starting deletion of deliveries for task ID: {}", taskId);

        Optional<Delivery> deliveryToDelete = deliveryRepository.findByTaskId(taskId);

        if (deliveryToDelete.isEmpty()) {
            log.debug("No delivery found for task ID: {}", taskId);
            return;
        }

        log.debug("Found delivery to delete for task ID: {}", taskId);

        Delivery delivery = deliveryToDelete.get();
        Long deliveryId = delivery.getId();
        try {
            deliveryAttachmentService.deleteAllDeliveryAttachmentsAndFolder(deliveryId);
            deliveryItemAttachmentService.deleteAllDeliveryItemAttachmentsByDeliveryId(deliveryId);
            deliveryOperationalAttachmentService.deleteAllOperationalAttachmentsByDeliveryId(deliveryId);
            log.info("Successfully deleted all attachments for delivery {} (task {})", deliveryId, taskId);
        } catch (Exception e) {
            log.warn("Failed to delete attachments for delivery {} (task {}): {}", deliveryId, taskId, e.getMessage());
        }

        deliveryRepository.deleteByTaskId(taskId);
        log.debug("Successfully deleted delivery for task ID: {}", taskId);
    }

    @Override
    public Page<DeliveryResponse> findAllPaginated(Long id,
                                                   String taskName,
                                                   String taskCode,
                                                   String flowType,
                                                   String status,
                                                   String createdAt,
                                                   String updatedAt,
                                                   Pageable pageable) {

        DeliveryStatus statusEnum = convertStatusStringToEnum(status);

        Page<Long> idsPage = deliveryRepository.findIdsByOptionalFieldsPaginated(
                id, taskName, taskCode, flowType, null, null, statusEnum, null, null, createdAt, updatedAt, pageable
        );

        if (idsPage.isEmpty()) {
            return idsPage.map(deliveryId -> null);
        }

        List<Delivery> deliveries = deliveryRepository.findByIdsWithEntityGraph(idsPage.getContent());

        List<DeliveryResponse> responses = deliveries.stream()
                .map(DeliveryAdapter::toResponseDTO)
                .toList();

        return new PageImpl<>(responses, pageable, idsPage.getTotalElements());
    }

    private DeliveryStatus convertStatusStringToEnum(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return null;
        }
        
        String upperStatus = statusString.toUpperCase().trim();

        try {
            return DeliveryStatus.valueOf(upperStatus);
        } catch (IllegalArgumentException ignored) {
        }
        

        for (DeliveryStatus deliveryStatus : DeliveryStatus.values()) {
            if (deliveryStatus.name().startsWith(upperStatus)) {
                log.debug("Partial match found: '{}' matches '{}'", statusString, deliveryStatus);
                return deliveryStatus;
            }
        }

        Map<String, DeliveryStatus> portugueseMapping = new HashMap<>();
        portugueseMapping.put("PEND", DeliveryStatus.PENDING);
        portugueseMapping.put("DESEN", DeliveryStatus.DEVELOPMENT);
        portugueseMapping.put("DESENV", DeliveryStatus.DEVELOPMENT);
        portugueseMapping.put("ENT", DeliveryStatus.DELIVERED);
        portugueseMapping.put("ENTREG", DeliveryStatus.DELIVERED);
        portugueseMapping.put("HOM", DeliveryStatus.HOMOLOGATION);
        portugueseMapping.put("HOMO", DeliveryStatus.HOMOLOGATION);
        portugueseMapping.put("APR", DeliveryStatus.APPROVED);
        portugueseMapping.put("APRO", DeliveryStatus.APPROVED);
        portugueseMapping.put("REJ", DeliveryStatus.REJECTED);
        portugueseMapping.put("PROD", DeliveryStatus.PRODUCTION);

        for (Map.Entry<String, DeliveryStatus> entry : portugueseMapping.entrySet()) {
            if (entry.getKey().startsWith(upperStatus) || upperStatus.startsWith(entry.getKey())) {
                log.debug("Portuguese partial match found: '{}' matches '{}'", statusString, entry.getValue());
                return entry.getValue();
            }
        }
        
        log.debug("No match found for status: '{}'", statusString);
        return null;
    }

    @Override
    public Page<DeliveryGroupResponse> findAllGroupedByTask(Long taskId,
                                                             String taskName,
                                                             String taskCode,
                                                             String flowType,
                                                             String taskType,
                                                             String environment,
                                                             String status,
                                                             String startDate,
                                                             String endDate,
                                                             String createdAt,
                                                             String updatedAt,
                                                             Pageable pageable) {

        log.debug("findAllGroupedByTask - taskId: {}, taskName: {}, taskCode: {}, flowType: {}, taskType: {}, environment: {}, status: {}, startDate: {}, endDate: {}, pageable: {}",
                taskId, taskName, taskCode, flowType, taskType, environment, status, startDate, endDate, pageable);

        DeliveryStatus statusEnum = convertStatusStringToEnum(status);

        Page<Long> idsPage;
        try {
            if (taskId != null || taskName != null || taskCode != null || flowType != null || taskType != null || environment != null || statusEnum != null || startDate != null || endDate != null) {
                log.debug("Using filtered query for IDs");
                idsPage = deliveryRepository.findIdsByOptionalFieldsPaginated(
                        taskId, taskName, taskCode, flowType, taskType, environment, statusEnum, startDate, endDate, createdAt, updatedAt, pageable
                );
            } else {
                log.debug("Using simple query for IDs");
                idsPage = deliveryRepository.findAllOrderedByTaskIdDescPaginated(pageable);
            }

            log.debug("Found {} delivery IDs", idsPage.getTotalElements());
        } catch (Exception e) {
            log.error("Error in findAllGroupedByTask: {}", e.getMessage(), e);
            throw e;
        }

        if (idsPage.isEmpty()) {
            return idsPage.map(id -> null);
        }

        List<Delivery> deliveries = deliveryRepository.findByIdsWithEntityGraph(idsPage.getContent());

        List<DeliveryGroupResponse> responses = deliveries.stream().map(delivery -> {
            Task task = null;
            if (delivery.getTask() != null) {
                task = delivery.getTask();
                task.getId();
            }

            String calculatedStatus = "PENDING";
            int totalItems = 0;

            if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
                totalItems += delivery.getItems().size();
            }

            if (delivery.getOperationalItems() != null && !delivery.getOperationalItems().isEmpty()) {
                totalItems += delivery.getOperationalItems().size();
            }

            if (totalItems > 0) {
                delivery.updateStatus();
                calculatedStatus = delivery.getStatus().name();
            }

            return DeliveryGroupResponse.builder()
                    .taskId(task != null ? task.getId() : null)
                    .taskName(task != null ? task.getTitle() : null)
                    .taskCode(task != null ? task.getCode() : null)
                    .taskType(task != null ? task.getTaskType() : null)
                    .deliveryStatus(calculatedStatus)
                    .calculatedDeliveryStatus(calculatedStatus)
                    .totalItems(totalItems)
                    .taskValue(task != null ? task.getAmount() : null)
                    .createdAt(delivery.getCreatedAt())
                    .updatedAt(delivery.getUpdatedAt())
                    .totalDeliveries(1)
                    .completedDeliveries(0)
                    .pendingDeliveries(0)
                    .deliveries(List.of(DeliveryAdapter.toResponseDTO(delivery)))
                    .build();
        }).toList();

        return new PageImpl<>(responses, pageable, idsPage.getTotalElements());
    }

    @Override
    public DeliveryGroupResponse findGroupDetailsByTaskId(Long taskId) {

        Optional<Delivery> deliveryOpt = deliveryRepository.findByTaskId(taskId);

        if (deliveryOpt.isEmpty()) {
            throw new RuntimeException("No delivery found for task ID: " + taskId);
        }

        Delivery delivery = deliveryOpt.get();

        if (delivery.getTask() != null) {
            delivery.getTask().getId();
        }
        if (delivery.getItems() != null) {
            delivery.getItems().size();
        }
        
        Task task = delivery.getTask();

        DeliveryResponse deliveryResponse = DeliveryAdapter.toResponseDTO(delivery);
        List<DeliveryResponse> deliveryResponses = List.of(deliveryResponse);

        long pendingCount = 0;
        long developmentCount = 0;
        long deliveredCount = 0;
        long homologationCount = 0;
        long approvedCount = 0;
        long rejectedCount = 0;
        long productionCount = 0;
        
        if (delivery.getItems() != null) {
            pendingCount = delivery.getItemsByStatus(DeliveryStatus.PENDING);
            developmentCount = delivery.getItemsByStatus(DeliveryStatus.DEVELOPMENT);
            deliveredCount = delivery.getItemsByStatus(DeliveryStatus.DELIVERED);
            homologationCount = delivery.getItemsByStatus(DeliveryStatus.HOMOLOGATION);
            approvedCount = delivery.getItemsByStatus(DeliveryStatus.APPROVED);
            rejectedCount = delivery.getItemsByStatus(DeliveryStatus.REJECTED);
            productionCount = delivery.getItemsByStatus(DeliveryStatus.PRODUCTION);
        }

       DeliveryStatusCount statusCounts = DeliveryStatusCount.builder()
                .pending((int) pendingCount)
                .development((int) developmentCount)
                .delivered((int) deliveredCount)
                .homologation((int) homologationCount)
                .approved((int) approvedCount)
                .rejected((int) rejectedCount)
                .production((int) productionCount)
                .build();

        long completedCount = approvedCount + productionCount;
        long totalPendingCount = delivery.getTotalItems() - completedCount;

        return DeliveryGroupResponse.builder()
                .taskId(taskId)
                .taskName(task.getTitle())
                .taskCode(task.getCode())
                .taskType(task.getTaskType())
                .deliveryStatus(delivery.getStatus().name())
                .taskValue(task.getAmount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .statusCounts(statusCounts)
                .totalDeliveries(1)
                .completedDeliveries((int) completedCount)
                .pendingDeliveries((int) totalPendingCount)
                .deliveries(deliveryResponses)
                .build();
    }

    @Override
    public byte[] exportToExcel(String flowType, boolean canViewAmounts) throws IOException {
        if ("OPERACIONAL".equals(flowType)) {
            return exportOperationalToExcel(canViewAmounts);
        } else {
            return exportDevelopmentToExcel(canViewAmounts);
        }
    }

    private byte[] exportDevelopmentToExcel(boolean canViewAmounts) throws IOException {
        log.debug("EXCEL EXPORT (Development Deliveries) STARTED canViewAmounts={}", canViewAmounts);

        String sql = """
            SELECT
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.task_type as task_type,
                t.environment as task_environment,
                t.amount as task_amount,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                d.status as delivery_status,
                d.notes as delivery_notes,
                d.started_at as delivery_started_at,
                d.finished_at as delivery_finished_at,
                p.name as project_name,
                di.status as item_status,
                di.branch as item_branch,
                di.source_branch as item_source_branch,
                di.pull_request as item_pull_request,
                di.notes as item_notes,
                di.started_at as item_started_at,
                di.finished_at as item_finished_at
            FROM delivery d
            INNER JOIN task t ON d.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN delivery_item di ON di.delivery_id = d.id
            LEFT JOIN project p ON di.project_id = p.id
            WHERE t.flow_type = 'DESENVOLVIMENTO'
            ORDER BY t.id DESC, d.id DESC, di.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_type", row[3]);
            map.put("task_environment", row[4]);
            map.put("task_amount", row[5]);
            map.put("subtasks_count", row[6]);
            map.put("requester_name", row[7]);
            map.put("delivery_status", row[8]);
            map.put("delivery_notes", row[9]);
            map.put("delivery_started_at", row[10]);
            map.put("delivery_finished_at", row[11]);
            map.put("project_name", row[12]);
            map.put("item_status", row[13]);
            map.put("item_branch", row[14]);
            map.put("item_source_branch", row[15]);
            map.put("item_pull_request", row[16]);
            map.put("item_notes", row[17]);
            map.put("item_started_at", row[18]);
            map.put("item_finished_at", row[19]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateDeliveriesReport(data, canViewAmounts);
    }

    private byte[] exportOperationalToExcel(boolean canViewAmounts) throws IOException {
        log.debug("EXCEL EXPORT (Operational Deliveries) STARTED canViewAmounts={}", canViewAmounts);

        String sql = """
            SELECT
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.task_type as task_type,
                t.environment as task_environment,
                t.amount as task_amount,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                d.status as delivery_status,
                d.notes as delivery_notes,
                d.started_at as delivery_started_at,
                d.finished_at as delivery_finished_at,
                doi.title as item_title,
                doi.description as item_description,
                doi.status as item_status,
                doi.started_at as item_started_at,
                doi.finished_at as item_finished_at
            FROM delivery d
            INNER JOIN task t ON d.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN delivery_operational_item doi ON doi.delivery_id = d.id
            WHERE t.flow_type = 'OPERACIONAL'
            ORDER BY t.id DESC, d.id DESC, doi.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("task_type", row[3]);
            map.put("task_environment", row[4]);
            map.put("task_amount", row[5]);
            map.put("subtasks_count", row[6]);
            map.put("requester_name", row[7]);
            map.put("delivery_status", row[8]);
            map.put("delivery_notes", row[9]);
            map.put("delivery_started_at", row[10]);
            map.put("delivery_finished_at", row[11]);
            map.put("item_title", row[12]);
            map.put("item_description", row[13]);
            map.put("item_status", row[14]);
            map.put("item_started_at", row[15]);
            map.put("item_finished_at", row[16]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateOperationalDeliveriesReport(data, canViewAmounts);
    }

    @Override
    public boolean existsByTaskId(Long taskId) {
        return deliveryRepository.existsByTaskId(taskId);
    }

    @Override
    public DeliveryStatusCount getGlobalStatistics() {
        log.debug("Executando query de estatísticas globais...");

        List<Delivery> allDeliveries = deliveryRepository.findAll();
        log.debug("Total de deliveries no banco: {}", allDeliveries.size());
        
        Map<DeliveryStatus, Integer> counts = new HashMap<>();
        for (Delivery delivery : allDeliveries) {
            counts.put(delivery.getStatus(), counts.getOrDefault(delivery.getStatus(), 0) + 1);
        }
        log.debug("Contagem manual por status: {}", counts);

        Object[] result = deliveryRepository.findGlobalDeliveryStatistics();
        log.debug("Resultado da query: {}", result != null ? Arrays.toString(result) : "null");
        
        if (result == null || result.length < 7) {
            log.debug("Query retornou dados insuficientes. Usando contagem manual.");
            return DeliveryStatusCount.builder()
                    .pending(counts.getOrDefault(DeliveryStatus.PENDING, 0))
                    .development(counts.getOrDefault(DeliveryStatus.DEVELOPMENT, 0))
                    .delivered(counts.getOrDefault(DeliveryStatus.DELIVERED, 0))
                    .homologation(counts.getOrDefault(DeliveryStatus.HOMOLOGATION, 0))
                    .approved(counts.getOrDefault(DeliveryStatus.APPROVED, 0))
                    .rejected(counts.getOrDefault(DeliveryStatus.REJECTED, 0))
                    .production(counts.getOrDefault(DeliveryStatus.PRODUCTION, 0))
                    .build();
        }
        
        DeliveryStatusCount stats = DeliveryStatusCount.builder()
                .pending(result[0] != null ? ((Number) result[0]).intValue() : 0)
                .development(result[1] != null ? ((Number) result[1]).intValue() : 0)
                .delivered(result[2] != null ? ((Number) result[2]).intValue() : 0)
                .homologation(result[3] != null ? ((Number) result[3]).intValue() : 0)
                .approved(result[4] != null ? ((Number) result[4]).intValue() : 0)
                .rejected(result[5] != null ? ((Number) result[5]).intValue() : 0)
                .production(result[6] != null ? ((Number) result[6]).intValue() : 0)
                .build();
        
        log.debug("Estatísticas calculadas pela query: {}", stats);
        log.debug("Estatísticas calculadas manualmente: pending={}, development={}", 
            counts.getOrDefault(DeliveryStatus.PENDING, 0), 
            counts.getOrDefault(DeliveryStatus.DEVELOPMENT, 0));
        
        return stats;
    }

    @Override
    @Transactional
    public void updateAllDeliveryStatuses() {
        log.debug("Atualizando status de todas as entregas...");

        List<Delivery> deliveries = deliveryRepository.findAll();
        int updated = 0;

        for (Delivery delivery : deliveries) {
            DeliveryStatus oldStatus = delivery.getStatus();
            delivery.updateStatus();
            DeliveryStatus newStatus = delivery.getStatus();

            if (oldStatus != newStatus) {
                deliveryRepository.save(delivery);
                updated++;
                log.debug("Delivery ID {} status updated: {} -> {}",
                    delivery.getId(), oldStatus, newStatus);
            }
        }

        log.debug("Status de {} entregas foram atualizados", updated);
    }

    public Page<DeliveryGroupResponse> findAllGroupedByTaskOptimized(String taskName, String taskCode,
                                                                     String status, String createdAt,
                                                                     String updatedAt, Pageable pageable) {

        return findAllGroupedByTask(null, taskName, taskCode, null, null, null, status, null, null, createdAt, updatedAt, pageable);
    }


    public DeliveryGroupResponse findGroupDetailsByTaskIdOptimized(Long taskId) {
        try {

            Object[] groupData = deliveryRepository.findDeliveryGroupByTaskIdOptimized(taskId);
            if (groupData == null) {
                throw new RuntimeException("No deliveries found for task ID: " + taskId);
            }

            Optional<Delivery> deliveryOpt = deliveryRepository.findByTaskId(taskId);
            List<DeliveryResponse> deliveryResponses = deliveryOpt
                    .map(DeliveryAdapter::toResponseDTO)
                    .map(List::of)
                    .orElse(List.of());

            DeliveryGroupResponse response = mapRowToDeliveryGroupResponse(groupData);
            response.setDeliveries(deliveryResponses);
            
            return response;
        } catch (Exception e) {
            log.warn("Erro ao buscar grupo otimizado para task ID {}, usando método tradicional: {}", taskId, e.getMessage());
            return findGroupDetailsByTaskId(taskId);
        }
    }


    private DeliveryGroupResponse mapRowToDeliveryGroupResponse(Object[] row) {
        try {
            log.debug("Mapeando resultado da query com {} elementos", row.length);
            for (int i = 0; i < row.length; i++) {
                log.debug("Índice {}: {} (Tipo: {})", i, row[i], row[i] != null ? row[i].getClass().getSimpleName() : "null");
            }

            return DeliveryGroupResponse.builder()
                    .taskId(safeGetLong(row[2]))
                    .taskName((String) row[3])
                    .taskCode((String) row[4])
                    .taskType((String) row[5])
                    .taskValue(row[6] != null ? new BigDecimal(row[6].toString()) : null)
                    .createdAt(safeGetTimestamp(row[7]))
                    .updatedAt(safeGetTimestamp(row[8]))
                    .deliveryStatus((String) row[1])
                    .statusCounts(DeliveryStatusCount.builder()
                            .pending(safeGetInteger(row[10]))
                            .development(safeGetInteger(row[11]))
                            .delivered(safeGetInteger(row[12]))
                            .homologation(safeGetInteger(row[13]))
                            .approved(safeGetInteger(row[14]))
                            .rejected(safeGetInteger(row[15]))
                            .production(safeGetInteger(row[16]))
                            .build())
                    .totalDeliveries(safeGetInteger(row[9]))
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear resultado da query: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar dados do grupo de entregas", e);
        }
    }

    @Override
    public DeliveryResponse findByTaskId(Long taskId) {
        
        return deliveryRepository.findByTaskId(taskId)
                .map(DeliveryAdapter::toResponseDTO)
                .orElse(null);
    }
    
    @Override
    @Transactional
    public void sendDeliveryEmail(Long id, List<String> additionalEmails, List<String> additionalWhatsAppRecipients, boolean sendEmail, boolean sendWhatsApp) {

        final Delivery delivery = deliveryRepository.findById(id)
                .map(entity -> {
                    if (entity.getTask() != null) {
                        entity.getTask().getTitle();
                        entity.getTask().getCode();
                        if (entity.getTask().getRequester() != null) {
                            entity.getTask().getRequester().getName();
                            entity.getTask().getRequester().getEmail();
                        }
                    }
                    if (entity.getItems() != null) {
                        entity.getItems().forEach(item -> {
                            item.getStatus();
                            if (item.getProject() != null) {
                                item.getProject().getName();
                            }
                        });
                    }
                    if (entity.getOperationalItems() != null) {
                        entity.getOperationalItems().forEach(item -> {
                            item.getStatus();
                            item.getTitle();
                            item.getDescription();
                        });
                    }
                    return entity;
                })
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada com ID: " + id));

        Map<String, byte[]> attachmentDataMap = new HashMap<>();

        try {

            List<DeliveryAttachment> deliveryAttachments = deliveryAttachmentService.getDeliveryAttachmentsEntities(id);
            if (deliveryAttachments != null && !deliveryAttachments.isEmpty()) {

                for (DeliveryAttachment attachment : deliveryAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("delivery_" + attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download delivery attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }

            List<DeliveryItemAttachment> itemAttachments = deliveryItemAttachmentService.getDeliveryItemAttachmentsEntitiesByDeliveryId(id);
            if (itemAttachments != null && !itemAttachments.isEmpty()) {
                for (DeliveryItemAttachment attachment : itemAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("item_" + attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download item attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }

            List<DeliveryOperationalAttachment> operationalAttachments = deliveryOperationalAttachmentService.getOperationalAttachmentsEntitiesByDeliveryId(id);
            if (operationalAttachments != null && !operationalAttachments.isEmpty()) {
                for (DeliveryOperationalAttachment attachment : operationalAttachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put("operational_" + attachment.getOriginalName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download operational attachment {} from S3: {}", attachment.getOriginalName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing attachments from database: {}", e.getMessage());
        }

        if (sendEmail) {
            try {
                if (!attachmentDataMap.isEmpty()) {
                    emailService.sendDeliveryUpdatedNotificationWithAttachmentData(delivery, attachmentDataMap, additionalEmails);
                } else {
                    emailService.sendDeliveryUpdatedNotification(delivery, additionalEmails);
                }
            } catch (Exception e) {
                log.error("FAILED to send delivery email for delivery ID: {} - Error: {}", id, e.getMessage());
            }
        }

        if (sendWhatsApp) {
            try {
                emailService.sendDeliveryNotificationWhatsApp(delivery, additionalWhatsAppRecipients);
            } catch (Exception e) {
                log.error("Failed to send delivery WhatsApp notification for delivery {}: {}", id, e.getMessage(), e);
            }
        }

        delivery.setDeliveryEmailSent(true);
        deliveryRepository.save(delivery);
    }
    
    private Long safeGetLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }
    
    private Integer safeGetInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }
    
    private LocalDateTime safeGetTimestamp(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }

    @Override
    @Transactional
    public DeliveryResponse updateNotes(Long id, String notes) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + id));

        entity.setNotes(notes);

        entity = deliveryRepository.save(entity);

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    @Transactional
    public DeliveryResponse updateEnvironment(Long id, br.com.devquote.enums.Environment environment) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + id));

        entity.setEnvironment(environment);

        entity = deliveryRepository.save(entity);

        return DeliveryAdapter.toResponseDTO(entity);
    }
}
