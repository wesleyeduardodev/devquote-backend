package br.com.devquote.adapter;

import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.enums.DeliveryStatus;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public final class DeliveryAdapter {

    public static DeliveryResponse toResponseDTO(Delivery entity) {
        if (entity == null) return null;

        return DeliveryResponse.builder()
                .id(entity.getId())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskName(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .taskCode(entity.getTask() != null ? entity.getTask().getCode() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .totalItems(entity.getTotalItems())
                .pendingCount(entity.getItemsByStatus(DeliveryStatus.PENDING))
                .developmentCount(entity.getItemsByStatus(DeliveryStatus.DEVELOPMENT))
                .deliveredCount(entity.getItemsByStatus(DeliveryStatus.DELIVERED))
                .homologationCount(entity.getItemsByStatus(DeliveryStatus.HOMOLOGATION))
                .approvedCount(entity.getItemsByStatus(DeliveryStatus.APPROVED))
                .rejectedCount(entity.getItemsByStatus(DeliveryStatus.REJECTED))
                .productionCount(entity.getItemsByStatus(DeliveryStatus.PRODUCTION))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deliveryEmailSent(entity.getDeliveryEmailSent())
                .items(entity.getItems() != null ? 
                        DeliveryItemAdapter.toResponseDTOList(entity.getItems()) : null)
                .build();
    }

    public static List<DeliveryResponse> toResponseDTOList(List<Delivery> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static Delivery toEntity(DeliveryRequest dto, Task task) {
        if (dto == null) return null;

        Delivery delivery = Delivery.builder()
                .task(task)
                .status(dto.getStatus() != null ? DeliveryStatus.fromString(dto.getStatus()) : DeliveryStatus.PENDING)
                .build();

        // Adicionar itens se fornecidos
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            dto.getItems().forEach(itemDto -> {
                var item = DeliveryItemAdapter.toEntity(itemDto, delivery, null); // Project será definido no service
                delivery.addItem(item);
            });
        }

        return delivery;
    }

    public static void updateEntityFromDto(DeliveryRequest dto, Delivery entity, Task task) {
        if (dto == null || entity == null) return;

        if (task != null) entity.setTask(task);
        
        if (dto.getStatus() != null) {
            entity.setStatus(DeliveryStatus.fromString(dto.getStatus()));
        }

        // Atualizar itens será gerenciado separadamente no service
        // pois envolve lógica mais complexa de CRUD dos DeliveryItems
    }
}
