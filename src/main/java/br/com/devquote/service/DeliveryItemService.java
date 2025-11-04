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

    List<DeliveryItemResponse> findByDeliveryId(Long deliveryId);

    List<DeliveryItemResponse> findByTaskId(Long taskId);

    List<DeliveryItemResponse> findByProjectId(Long projectId);

    List<DeliveryItemResponse> findByStatus(DeliveryStatus status);

    long countByDeliveryId(Long deliveryId);
    long countByDeliveryIdAndStatus(Long deliveryId, DeliveryStatus status);

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

    List<DeliveryItemResponse> findItemsByTaskIdOptimized(Long taskId);

    byte[] exportToExcel() throws IOException;

    List<DeliveryItemResponse> createMultipleItems(Long deliveryId, List<DeliveryItemRequest> items);

    List<DeliveryItemResponse> updateMultipleItems(List<Long> itemIds, List<DeliveryItemRequest> items);
}