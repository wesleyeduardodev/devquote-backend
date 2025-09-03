package br.com.devquote.service.impl;

import br.com.devquote.adapter.DeliveryItemAdapter;
import br.com.devquote.dto.request.DeliveryItemRequest;
import br.com.devquote.dto.response.DeliveryItemResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.entity.Project;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.repository.DeliveryItemRepository;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.service.DeliveryItemService;
import br.com.devquote.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryItemServiceImpl implements DeliveryItemService {

    private final DeliveryItemRepository deliveryItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;

    @Override
    public List<DeliveryItemResponse> findAll() {
        log.debug("Finding all delivery items");
        var items = deliveryItemRepository.findAll();
        return DeliveryItemAdapter.toResponseDTOList(items);
    }

    @Override
    public DeliveryItemResponse findById(Long id) {
        log.debug("Finding delivery item by id: {}", id);
        var item = deliveryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeliveryItem not found with id: " + id));
        return DeliveryItemAdapter.toResponseDTO(item);
    }

    @Override
    @Transactional
    public DeliveryItemResponse create(DeliveryItemRequest dto) {
        log.debug("Creating delivery item: {}", dto);

        // Buscar delivery
        Delivery delivery = deliveryRepository.findById(dto.getDeliveryId())
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + dto.getDeliveryId()));

        // Buscar projeto
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + dto.getProjectId()));

        // Criar item
        DeliveryItem item = DeliveryItemAdapter.toEntity(dto, delivery, project);
        item = deliveryItemRepository.save(item);

        // Atualizar status da delivery automaticamente
        delivery.updateStatus();
        deliveryRepository.save(delivery);

        log.info("DeliveryItem created with id: {}", item.getId());
        return DeliveryItemAdapter.toResponseDTO(item);
    }

    @Override
    @Transactional
    public DeliveryItemResponse update(Long id, DeliveryItemRequest dto) {
        log.debug("Updating delivery item id: {} with: {}", id, dto);

        DeliveryItem item = deliveryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeliveryItem not found with id: " + id));

        // Buscar delivery se mudou
        Delivery delivery = null;
        if (!item.getDelivery().getId().equals(dto.getDeliveryId())) {
            delivery = deliveryRepository.findById(dto.getDeliveryId())
                    .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + dto.getDeliveryId()));
        }

        // Buscar projeto se mudou
        Project project = null;
        if (!item.getProject().getId().equals(dto.getProjectId())) {
            project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + dto.getProjectId()));
        }

        // Atualizar item
        DeliveryItemAdapter.updateEntityFromDto(dto, item, delivery, project);
        item = deliveryItemRepository.save(item);

        // Atualizar status da delivery automaticamente
        item.getDelivery().updateStatus();
        deliveryRepository.save(item.getDelivery());

        log.info("DeliveryItem updated with id: {}", item.getId());
        return DeliveryItemAdapter.toResponseDTO(item);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting delivery item with id: {}", id);

        DeliveryItem item = deliveryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeliveryItem not found with id: " + id));

        Delivery delivery = item.getDelivery();
        deliveryItemRepository.delete(item);

        // Atualizar status da delivery automaticamente
        delivery.updateStatus();
        deliveryRepository.save(delivery);

        log.info("DeliveryItem deleted with id: {}", id);
    }

    @Override
    @Transactional
    public void deleteBulk(List<Long> ids) {
        log.debug("Bulk deleting delivery items: {}", ids);

        List<DeliveryItem> items = deliveryItemRepository.findAllById(ids);
        if (items.size() != ids.size()) {
            throw new RuntimeException("Some delivery items not found");
        }

        // Coletar deliveries afetadas para atualizar status
        var affectedDeliveries = items.stream()
                .map(DeliveryItem::getDelivery)
                .distinct()
                .toList();

        deliveryItemRepository.deleteAll(items);

        // Atualizar status das deliveries afetadas
        affectedDeliveries.forEach(delivery -> {
            delivery.updateStatus();
            deliveryRepository.save(delivery);
        });

        log.info("Bulk deleted {} delivery items", ids.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryItemResponse> findByDeliveryId(Long deliveryId) {
        log.debug("Finding delivery items by delivery id: {}", deliveryId);
        var items = deliveryItemRepository.findByDeliveryId(deliveryId);
        return DeliveryItemAdapter.toResponseDTOList(items);
    }

    @Override
    public List<DeliveryItemResponse> findByTaskId(Long taskId) {
        log.debug("Finding delivery items by task id: {}", taskId);
        var items = deliveryItemRepository.findByTaskId(taskId);
        return DeliveryItemAdapter.toResponseDTOList(items);
    }

    @Override
    public List<DeliveryItemResponse> findByProjectId(Long projectId) {
        log.debug("Finding delivery items by project id: {}", projectId);
        var items = deliveryItemRepository.findByProjectId(projectId);
        return DeliveryItemAdapter.toResponseDTOList(items);
    }

    @Override
    public List<DeliveryItemResponse> findByStatus(DeliveryStatus status) {
        log.debug("Finding delivery items by status: {}", status);
        var items = deliveryItemRepository.findByStatus(status);
        return DeliveryItemAdapter.toResponseDTOList(items);
    }

    @Override
    public long countByDeliveryId(Long deliveryId) {
        return deliveryItemRepository.countByDeliveryId(deliveryId);
    }

    @Override
    public long countByDeliveryIdAndStatus(Long deliveryId, DeliveryStatus status) {
        return deliveryItemRepository.countByDeliveryIdAndStatus(deliveryId, status);
    }

    @Override
    public long countByTaskId(Long taskId) {
        return deliveryItemRepository.countByTaskId(taskId);
    }

    @Override
    public long countByTaskIdAndStatus(Long taskId, DeliveryStatus status) {
        return deliveryItemRepository.countByTaskIdAndStatus(taskId, status);
    }

    @Override
    public Page<DeliveryItemResponse> findAllPaginated(Long id, Long deliveryId, Long taskId, String taskName,
                                                       String taskCode, String projectName, String branch,
                                                       String pullRequest, DeliveryStatus status, String startedAt,
                                                       String finishedAt, String createdAt, String updatedAt,
                                                       Pageable pageable) {
        log.debug("Finding delivery items paginated with filters");
        
        Page<DeliveryItem> page = deliveryItemRepository.findByOptionalFieldsPaginated(
                id, deliveryId, taskId, taskName, taskCode, projectName, branch, pullRequest,
                status, startedAt, finishedAt, createdAt, updatedAt, pageable
        );

        return page.map(DeliveryItemAdapter::toResponseDTO);
    }

    @Override
    public List<DeliveryItemResponse> findItemsByTaskIdOptimized(Long taskId) {
        log.debug("Finding delivery items by task id optimized: {}", taskId);
        
        List<Object[]> results = deliveryItemRepository.findItemsByTaskIdOptimized(taskId);
        
        return results.stream().map(row -> DeliveryItemResponse.builder()
                .id(((Number) row[0]).longValue())
                .deliveryId(((Number) row[1]).longValue())
                .projectId(((Number) row[2]).longValue())
                .status((String) row[3])
                .branch((String) row[4])
                .sourceBranch((String) row[5])
                .pullRequest((String) row[6])
                .script((String) row[7])
                .notes((String) row[8])
                .startedAt(row[9] != null ? ((java.sql.Date) row[9]).toLocalDate() : null)
                .finishedAt(row[10] != null ? ((java.sql.Date) row[10]).toLocalDate() : null)
                .createdAt(row[11] != null ? ((java.sql.Timestamp) row[11]).toLocalDateTime() : null)
                .updatedAt(row[12] != null ? ((java.sql.Timestamp) row[12]).toLocalDateTime() : null)
                .projectName((String) row[16])
                .taskId(((Number) row[17]).longValue())
                .taskName((String) row[18])
                .taskCode((String) row[19])
                .build()).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public List<DeliveryItemResponse> createMultipleItems(Long deliveryId, List<DeliveryItemRequest> items) {
        log.debug("Creating multiple delivery items for delivery: {}", deliveryId);

        // Buscar delivery
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        List<DeliveryItem> createdItems = new ArrayList<>();

        for (DeliveryItemRequest itemDto : items) {
            // Validar que o deliveryId bate
            if (!deliveryId.equals(itemDto.getDeliveryId())) {
                throw new RuntimeException("DeliveryId mismatch in item request");
            }

            // Buscar projeto
            Project project = projectRepository.findById(itemDto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + itemDto.getProjectId()));

            // Criar item
            DeliveryItem item = DeliveryItemAdapter.toEntity(itemDto, delivery, project);
            createdItems.add(item);
        }

        // Salvar todos os itens
        createdItems = deliveryItemRepository.saveAll(createdItems);

        // Atualizar status da delivery automaticamente
        delivery.updateStatus();
        deliveryRepository.save(delivery);

        log.info("Created {} delivery items for delivery: {}", createdItems.size(), deliveryId);
        return DeliveryItemAdapter.toResponseDTOList(createdItems);
    }

    @Override
    @Transactional
    public List<DeliveryItemResponse> updateMultipleItems(List<Long> itemIds, List<DeliveryItemRequest> items) {
        log.debug("Updating multiple delivery items: {}", itemIds);

        if (itemIds.size() != items.size()) {
            throw new RuntimeException("Item IDs and requests count mismatch");
        }

        List<DeliveryItem> updatedItems = new ArrayList<>();

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            DeliveryItemRequest itemDto = items.get(i);

            DeliveryItem item = deliveryItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("DeliveryItem not found with id: " + itemId));

            // Buscar delivery se mudou
            Delivery delivery = null;
            if (!item.getDelivery().getId().equals(itemDto.getDeliveryId())) {
                delivery = deliveryRepository.findById(itemDto.getDeliveryId())
                        .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + itemDto.getDeliveryId()));
            }

            // Buscar projeto se mudou
            Project project = null;
            if (!item.getProject().getId().equals(itemDto.getProjectId())) {
                project = projectRepository.findById(itemDto.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found with id: " + itemDto.getProjectId()));
            }

            // Atualizar item
            DeliveryItemAdapter.updateEntityFromDto(itemDto, item, delivery, project);
            updatedItems.add(item);
        }

        // Salvar todas as alterações
        updatedItems = deliveryItemRepository.saveAll(updatedItems);

        // Atualizar status das deliveries afetadas
        var affectedDeliveries = updatedItems.stream()
                .map(DeliveryItem::getDelivery)
                .distinct()
                .toList();

        affectedDeliveries.forEach(delivery -> {
            delivery.updateStatus();
            deliveryRepository.save(delivery);
        });

        log.info("Updated {} delivery items", itemIds.size());
        return DeliveryItemAdapter.toResponseDTOList(updatedItems);
    }

    @Override
    public byte[] exportToExcel() throws IOException {
        log.debug("Exporting delivery items to Excel");

        List<DeliveryItem> items = deliveryItemRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DeliveryItems");

            // Criar header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Delivery ID", "Task Name", "Task Code", "Project Name", 
                               "Status", "Branch", "Source Branch", "Pull Request", "Notes", 
                               "Started At", "Finished At", "Created At", "Updated At"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }

            // Preencher dados
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            IntStream.range(0, items.size()).forEach(i -> {
                DeliveryItem item = items.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getDelivery().getId());
                row.createCell(2).setCellValue(item.getDelivery().getTask().getTitle());
                row.createCell(3).setCellValue(item.getDelivery().getTask().getCode());
                row.createCell(4).setCellValue(item.getProject().getName());
                row.createCell(5).setCellValue(item.getStatus().getDisplayName());
                row.createCell(6).setCellValue(item.getBranch() != null ? item.getBranch() : "");
                row.createCell(7).setCellValue(item.getSourceBranch() != null ? item.getSourceBranch() : "");
                row.createCell(8).setCellValue(item.getPullRequest() != null ? item.getPullRequest() : "");
                row.createCell(9).setCellValue(item.getNotes() != null ? item.getNotes() : "");
                row.createCell(10).setCellValue(item.getStartedAt() != null ? item.getStartedAt().format(formatter) : "");
                row.createCell(11).setCellValue(item.getFinishedAt() != null ? item.getFinishedAt().format(formatter) : "");
                row.createCell(12).setCellValue(item.getCreatedAt() != null ? item.getCreatedAt().format(formatter) : "");
                row.createCell(13).setCellValue(item.getUpdatedAt() != null ? item.getUpdatedAt().format(formatter) : "");
            });

            // Auto-ajustar colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Converter para bytes
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
}