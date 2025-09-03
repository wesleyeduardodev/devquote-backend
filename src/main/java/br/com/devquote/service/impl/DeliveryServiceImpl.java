package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAdapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.DeliveryStatusCount;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Task;
import br.com.devquote.enums.DeliveryStatus;
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
        // Inicializar itens lazy se necessário
        if (entity.getItems() != null) {
            entity.getItems().size();
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse create(DeliveryRequest dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        // Verificar se já existe delivery para essa tarefa (relacionamento 1:1)
        if (deliveryRepository.existsByTaskId(dto.getTaskId())) {
            throw new RuntimeException("Delivery already exists for this task");
        }
        
        // Criar delivery sem itens primeiro
        Delivery entity = Delivery.builder()
                .task(task)
                .status(dto.getStatus() != null ? br.com.devquote.enums.DeliveryStatus.fromString(dto.getStatus()) : br.com.devquote.enums.DeliveryStatus.PENDING)
                .build();
        
        // Salvar delivery primeiro para ter ID gerado
        entity = deliveryRepository.save(entity);
        
        // Adicionar itens se fornecidos, fazendo lookup do projeto
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            final Delivery savedDelivery = entity;
            dto.getItems().forEach(itemDto -> {
                Project project = projectRepository.findById(itemDto.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found with ID: " + itemDto.getProjectId()));
                
                var item = br.com.devquote.entity.DeliveryItem.builder()
                        .delivery(savedDelivery)
                        .project(project)
                        .status(itemDto.getStatus() != null ? br.com.devquote.enums.DeliveryStatus.fromString(itemDto.getStatus()) : br.com.devquote.enums.DeliveryStatus.PENDING)
                        .branch(itemDto.getBranch())
                        .sourceBranch(itemDto.getSourceBranch())
                        .pullRequest(itemDto.getPullRequest())
                        .script(itemDto.getScript())
                        .notes(itemDto.getNotes())
                        .startedAt(itemDto.getStartedAt())
                        .finishedAt(itemDto.getFinishedAt())
                        .build();
                
                savedDelivery.addItem(item);
            });
            
            // Salvar novamente com os itens
            entity = deliveryRepository.save(entity);
        }

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
                    // Na nova arquitetura, inicializar itens da delivery
                    if (d.getItems() != null) {
                        d.getItems().size(); // Inicializa items
                        d.getItems().forEach(item -> {
                            if (item.getProject() != null) {
                                item.getProject().getName(); // Inicializa Project dos items
                            }
                        });
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
        // Na nova arquitetura, não há projectId direto no DeliveryRequest
        DeliveryAdapter.updateEntityFromDto(dto, entity, task);
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
                    // Na nova arquitetura, inicializar itens da delivery
                    if (d.getItems() != null) {
                        d.getItems().size(); // Inicializa items
                        d.getItems().forEach(item -> {
                            if (item.getProject() != null) {
                                item.getProject().getName(); // Inicializa Project dos items
                            }
                        });
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
                    // Na nova arquitetura, inicializar itens da delivery
                    if (d.getItems() != null) {
                        d.getItems().size(); // Inicializa items
                        d.getItems().forEach(item -> {
                            if (item.getProject() != null) {
                                item.getProject().getName(); // Inicializa Project dos items
                                log.debug("Project initialized from item: {}", item.getProject().getName());
                            }
                        });
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

        // Na nova arquitetura, buscar a delivery única da task antes de deletar
        Optional<Delivery> deliveryToDelete = deliveryRepository.findByTaskId(taskId);

        if (deliveryToDelete.isEmpty()) {
            log.info("No delivery found for task ID: {}", taskId);
            return;
        }

        log.info("Found delivery to delete for task ID: {}", taskId);

        // Enviar notificação por email para a delivery antes da exclusão
        Delivery delivery = deliveryToDelete.get();
        {
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
                        // Na nova arquitetura, inicializar itens da delivery
                        if (d.getItems() != null) {
                            d.getItems().size(); // Inicializa items
                            d.getItems().forEach(item -> {
                                if (item.getProject() != null) {
                                    item.getProject().getName(); // Inicializa Project dos items
                                    log.debug("Project initialized from item: {}", item.getProject().getName());
                                }
                            });
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
        log.info("Successfully deleted delivery for task ID: {}", taskId);
    }

    @Override
    public Page<DeliveryResponse> findAllPaginated(Long id,
                                                   String taskName,
                                                   String taskCode,
                                                   String status,
                                                   String createdAt,
                                                   String updatedAt,
                                                   Pageable pageable) {

        // Na nova arquitetura, a busca paginada tem menos parâmetros
        Page<Delivery> page = deliveryRepository.findByOptionalFieldsPaginated(
                id, taskName, taskCode, status, createdAt, updatedAt, pageable
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

        // Considera APPROVED e PRODUCTION como entregas finalizadas
        long completedCount = deliveries.stream()
                .filter(d -> "APPROVED".equals(d.getStatus()) || "PRODUCTION".equals(d.getStatus()))
                .count();

        if (completedCount == deliveries.size()) {
            return "COMPLETED"; // Todas aprovadas/em produção
        } else if (completedCount > 0) {
            return "IN_PROGRESS"; // Algumas finalizadas, outras não
        } else {
            return "PENDING"; // Nenhuma finalizada
        }
    }

    @Override
    public Page<DeliveryGroupResponse> findAllGroupedByTask(String taskName,
                                                             String taskCode,
                                                             String status,
                                                             String createdAt,
                                                             String updatedAt,
                                                             Pageable pageable) {

        log.info("findAllGroupedByTask - taskName: {}, taskCode: {}, status: {}, pageable: {}", 
                taskName, taskCode, status, pageable);

        // Buscar deliveries com paginação diretamente do repository (ordenado por task.id DESC)
        Page<Delivery> deliveryPage;
        try {
            if (taskName != null || taskCode != null || status != null) {
                // Se tem filtros, usa o método com filtros
                log.info("Using filtered query");
                deliveryPage = deliveryRepository.findByOptionalFieldsPaginated(
                        null, taskName, taskCode, status, createdAt, updatedAt, pageable
                );
            } else {
                // Se não tem filtros, usa o método simples
                log.info("Using simple query");
                deliveryPage = deliveryRepository.findAllOrderedByTaskIdDesc(pageable);
            }
            
            log.info("Found {} deliveries", deliveryPage.getTotalElements());
        } catch (Exception e) {
            log.error("Error in findAllGroupedByTask: {}", e.getMessage(), e);
            throw e;
        }

        // Converter para DeliveryGroupResponse de forma simples
        return deliveryPage.map(delivery -> {
            // Inicializar relacionamentos lazy de forma segura
            Task task = null;
            if (delivery.getTask() != null) {
                task = delivery.getTask();
                task.getId(); // Inicializa Task
            }
            
            // Calcular status e contagem de itens
            String calculatedStatus = "PENDING";
            int totalItems = 0;
            
            if (delivery.getItems() != null && !delivery.getItems().isEmpty()) {
                totalItems = delivery.getItems().size(); // Inicializa Items e conta
                delivery.updateStatus(); // Atualiza status baseado nos itens
                calculatedStatus = delivery.getStatus().name();
            }

            return DeliveryGroupResponse.builder()
                    .taskId(task != null ? task.getId() : null)
                    .taskName(task != null ? task.getTitle() : null)
                    .taskCode(task != null ? task.getCode() : null)
                    .deliveryStatus(calculatedStatus)
                    .calculatedDeliveryStatus(calculatedStatus) // Status calculado para exibição
                    .totalItems(totalItems) // Quantidade de itens
                    .taskValue(task != null ? task.getAmount() : null)
                    .createdAt(delivery.getCreatedAt())
                    .updatedAt(delivery.getUpdatedAt())
                    .totalDeliveries(1)
                    .completedDeliveries(0) // Simplificado
                    .pendingDeliveries(0)   // Simplificado
                    .deliveries(List.of(DeliveryAdapter.toResponseDTO(delivery))) // Incluir a entrega real
                    .build();
        });
    }

    @Override
    public DeliveryGroupResponse findGroupDetailsByTaskId(Long taskId) {
        // Na nova arquitetura, cada task tem apenas uma delivery
        Optional<Delivery> deliveryOpt = deliveryRepository.findByTaskId(taskId);

        if (deliveryOpt.isEmpty()) {
            throw new RuntimeException("No delivery found for task ID: " + taskId);
        }

        Delivery delivery = deliveryOpt.get();
        
        // Inicializar lazy relationships
        if (delivery.getTask() != null) {
            delivery.getTask().getId(); // Inicializa Task
        }
        if (delivery.getItems() != null) {
            delivery.getItems().size(); // Inicializa Items
        }
        
        Task task = delivery.getTask();

        // Na nova arquitetura, retornar a única delivery como resposta
        DeliveryResponse deliveryResponse = DeliveryAdapter.toResponseDTO(delivery);
        List<DeliveryResponse> deliveryResponses = List.of(deliveryResponse);

        // Calcular contadores por status baseado nos items da delivery (com verificação segura)
        long pendingCount = 0;
        long developmentCount = 0;
        long deliveredCount = 0;
        long homologationCount = 0;
        long approvedCount = 0;
        long rejectedCount = 0;
        long productionCount = 0;
        
        if (delivery.getItems() != null) {
            pendingCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.PENDING);
            developmentCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.DEVELOPMENT);
            deliveredCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.DELIVERED);
            homologationCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.HOMOLOGATION);
            approvedCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.APPROVED);
            rejectedCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.REJECTED);
            productionCount = delivery.getItemsByStatus(br.com.devquote.enums.DeliveryStatus.PRODUCTION);
        }
        
        // Criar statusCounts
        br.com.devquote.dto.response.DeliveryStatusCount statusCounts = br.com.devquote.dto.response.DeliveryStatusCount.builder()
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
                .deliveryStatus(delivery.getStatus().name())
                .taskValue(task.getAmount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .statusCounts(statusCounts)
                .totalDeliveries(1) // Sempre 1 na nova arquitetura
                .completedDeliveries((int) completedCount)
                .pendingDeliveries((int) totalPendingCount)
                .deliveries(deliveryResponses)
                .build();
    }

    @Override
    public byte[] exportToExcel() throws IOException {
        // Nova query adaptada para estrutura com DeliveryItem (sem IDs, valor e datas da entrega)
        String sql = """
            SELECT 
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                d.status as delivery_status,
                p.name as project_name,
                di.status as item_status,
                di.branch as item_branch,
                di.source_branch as item_source_branch,
                di.pull_request as item_pull_request,
                di.notes as item_notes,
                di.started_at as item_started_at,
                di.finished_at as item_finished_at,
                di.script as item_script
            FROM delivery d
            INNER JOIN task t ON d.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN delivery_item di ON di.delivery_id = d.id
            LEFT JOIN project p ON di.project_id = p.id
            ORDER BY t.id DESC, d.id DESC, di.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            // Dados da tarefa primeiro (colunas básicas)
            map.put("task_id", row[0]);
            map.put("task_code", row[1]);
            map.put("task_title", row[2]);
            map.put("subtasks_count", row[3]);
            map.put("requester_name", row[4]);
            
            // Dados da entrega (sem datas)
            map.put("delivery_status", row[5]);
            
            // Dados do item de entrega (podem ser múltiplos por tarefa)
            map.put("project_name", row[6]);
            map.put("item_status", row[7]);
            map.put("item_branch", row[8]);
            map.put("item_source_branch", row[9]);
            map.put("item_pull_request", row[10]);
            map.put("item_notes", row[11]);
            map.put("item_started_at", row[12]);
            map.put("item_finished_at", row[13]);
            map.put("item_script", row[14]); // Script por último
            
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateDeliveriesReport(data);
    }

    @Override
    public boolean existsByTaskId(Long taskId) {
        return deliveryRepository.existsByTaskId(taskId);
    }

    @Override
    public DeliveryStatusCount getGlobalStatistics() {
        log.info("Executando query de estatísticas globais...");
        
        // Método de debug: contar manualmente primeiro
        List<Delivery> allDeliveries = deliveryRepository.findAll();
        log.info("Total de deliveries no banco: {}", allDeliveries.size());
        
        Map<DeliveryStatus, Integer> counts = new HashMap<>();
        for (Delivery delivery : allDeliveries) {
            counts.put(delivery.getStatus(), counts.getOrDefault(delivery.getStatus(), 0) + 1);
        }
        log.info("Contagem manual por status: {}", counts);
        
        // Agora executar a query original
        Object[] result = deliveryRepository.findGlobalDeliveryStatistics();
        log.info("Resultado da query: {}", result != null ? Arrays.toString(result) : "null");
        
        if (result == null || result.length < 7) {
            log.warn("Query retornou dados insuficientes. Usando contagem manual.");
            // Usar contagem manual como fallback
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
        
        log.info("Estatísticas calculadas pela query: {}", stats);
        log.info("Estatísticas calculadas manualmente: pending={}, development={}", 
            counts.getOrDefault(DeliveryStatus.PENDING, 0), 
            counts.getOrDefault(DeliveryStatus.DEVELOPMENT, 0));
        
        return stats;
    }

    @Override
    @Transactional
    public void updateAllDeliveryStatuses() {
        log.info("Atualizando status de todas as entregas...");
        
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
        
        log.info("Status de {} entregas foram atualizados", updated);
    }

    /**
     * Método otimizado para buscar grupos de entregas com dados pré-calculados
     * Melhora significativa de performance comparado ao método original
     */
    public Page<DeliveryGroupResponse> findAllGroupedByTaskOptimized(String taskName, String taskCode, 
                                                                     String status, String createdAt, 
                                                                     String updatedAt, Pageable pageable) {
        
        // Na nova arquitetura, usar o método não otimizado que já funciona
        return findAllGroupedByTask(taskName, taskCode, status, createdAt, updatedAt, pageable);
    }

    /**
     * Método otimizado para buscar detalhes de um grupo específico
     */
    public DeliveryGroupResponse findGroupDetailsByTaskIdOptimized(Long taskId) {
        try {
            // Buscar dados básicos do grupo otimizados
            Object[] groupData = deliveryRepository.findDeliveryGroupByTaskIdOptimized(taskId);
            if (groupData == null) {
                throw new RuntimeException("No deliveries found for task ID: " + taskId);
            }
            
            // Na nova arquitetura, buscar a única delivery da task
            Optional<Delivery> deliveryOpt = deliveryRepository.findByTaskId(taskId);
            List<DeliveryResponse> deliveryResponses = deliveryOpt
                    .map(DeliveryAdapter::toResponseDTO)
                    .map(List::of)
                    .orElse(List.of());
            
            // Converter dados otimizados para DTO
            DeliveryGroupResponse response = mapRowToDeliveryGroupResponse(groupData);
            response.setDeliveries(deliveryResponses);
            
            return response;
        } catch (Exception e) {
            // Fallback para método original se houver problema com otimizado
            log.warn("Erro ao buscar grupo otimizado para task ID {}, usando método tradicional: {}", taskId, e.getMessage());
            return findGroupDetailsByTaskId(taskId);
        }
    }

    /**
     * Método helper para converter Object[] do resultado da query nativa para DeliveryGroupResponse
     */
    private DeliveryGroupResponse mapRowToDeliveryGroupResponse(Object[] row) {
        try {
            // Log para debug
            log.debug("Mapeando resultado da query com {} elementos", row.length);
            for (int i = 0; i < row.length; i++) {
                log.debug("Índice {}: {} (Tipo: {})", i, row[i], row[i] != null ? row[i].getClass().getSimpleName() : "null");
            }
            
            // Índices do resultado da query nativa:
            // 0: task_id, 1: task_name, 2: task_code, 3: task_value, 4: created_at, 5: updated_at,
            // 6: total_deliveries, 7: pending_count, 8: development_count, 9: delivered_count,
            // 10: homologation_count, 11: approved_count, 12: rejected_count, 13: production_count, 14: delivery_status
            
            return DeliveryGroupResponse.builder()
                    .taskId(safeGetLong(row[0]))
                    .taskName((String) row[1])
                    .taskCode((String) row[2])
                    .taskValue(row[3] != null ? new BigDecimal(row[3].toString()) : null)
                    .createdAt(safeGetTimestamp(row[4]))
                    .updatedAt(safeGetTimestamp(row[5]))
                    .deliveryStatus((String) row[14])
                    .statusCounts(br.com.devquote.dto.response.DeliveryStatusCount.builder()
                            .pending(safeGetInteger(row[7]))
                            .development(safeGetInteger(row[8]))
                            .delivered(safeGetInteger(row[9]))
                            .homologation(safeGetInteger(row[10]))
                            .approved(safeGetInteger(row[11]))
                            .rejected(safeGetInteger(row[12]))
                            .production(safeGetInteger(row[13]))
                            .build())
                    .totalDeliveries(safeGetInteger(row[6]))
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear resultado da query: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar dados do grupo de entregas", e);
        }
    }

    @Override
    public DeliveryResponse findByTaskId(Long taskId) {
        log.info("Buscando entrega por task ID: {}", taskId);
        
        return deliveryRepository.findByTaskId(taskId)
                .map(DeliveryAdapter::toResponseDTO)
                .orElse(null);
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
}
