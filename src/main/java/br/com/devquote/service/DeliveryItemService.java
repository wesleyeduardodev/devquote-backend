package br.com.devquote.service;

import br.com.devquote.dto.request.DeliveryItemRequest;
import br.com.devquote.dto.response.DeliveryItemResponse;
import br.com.devquote.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface DeliveryItemService {

    List<DeliveryItemResponse> findAll();

    DeliveryItemResponse findById(Long id);

    DeliveryItemResponse create(DeliveryItemRequest dto);

    DeliveryItemResponse update(Long id, DeliveryItemRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    // Buscar itens por delivery
    List<DeliveryItemResponse> findByDeliveryId(Long deliveryId);

    // Buscar itens por tarefa (via delivery)
    List<DeliveryItemResponse> findByTaskId(Long taskId);

    // Buscar itens por projeto
    List<DeliveryItemResponse> findByProjectId(Long projectId);

    // Buscar itens por status
    List<DeliveryItemResponse> findByStatus(DeliveryStatus status);

    // Contadores
    long countByDeliveryId(Long deliveryId);
    long countByDeliveryIdAndStatus(Long deliveryId, DeliveryStatus status);
    long countByTaskId(Long taskId);
    long countByTaskIdAndStatus(Long taskId, DeliveryStatus status);

    // Paginação com filtros
    Page<DeliveryItemResponse> findAllPaginated(
            Long id,
            Long deliveryId,
            Long taskId,
            String taskName,
            String taskCode,
            String projectName,
            String branch,
            String pullRequest,
            DeliveryStatus status,
            String startedAt,
            String finishedAt,
            String createdAt,
            String updatedAt,
            Pageable pageable
    );

    // Método otimizado para buscar itens por tarefa
    List<DeliveryItemResponse> findItemsByTaskIdOptimized(Long taskId);

    // Export
    byte[] exportToExcel() throws IOException;

    // Método para criar múltiplos itens para uma delivery
    List<DeliveryItemResponse> createMultipleItems(Long deliveryId, List<DeliveryItemRequest> items);

    // Método para atualizar múltiplos itens
    List<DeliveryItemResponse> updateMultipleItems(List<Long> itemIds, List<DeliveryItemRequest> items);
}