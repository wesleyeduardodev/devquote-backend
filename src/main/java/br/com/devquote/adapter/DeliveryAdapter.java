package br.com.devquote.adapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.dto.response.DeliveryOperationalItemResponse;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Task;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.enums.Environment;
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
                .taskType(entity.getTask() != null ? entity.getTask().getTaskType() : null)
                .flowType(entity.getFlowType() != null ? entity.getFlowType().name() : null)
                .environment(entity.getEnvironment() != null ? entity.getEnvironment().name() : null)
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
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .deliveryEmailSent(entity.getDeliveryEmailSent())
                .notes(entity.getNotes())
                .items(entity.getItems() != null ?
                        DeliveryItemAdapter.toResponseDTOList(entity.getItems()) : null)
                .operationalItems(entity.getOperationalItems() != null ?
                        entity.getOperationalItems().stream()
                                .map(item -> {
                                    List<DeliveryOperationalAttachmentResponse> attachments = item.getAttachments() != null
                                            ? item.getAttachments().stream()
                                                    .map(att -> DeliveryOperationalAttachmentResponse.builder()
                                                            .id(att.getId())
                                                            .deliveryOperationalItemId(item.getId())
                                                            .fileName(att.getFileName())
                                                            .originalName(att.getOriginalName())
                                                            .filePath(att.getFilePath())
                                                            .fileSize(att.getFileSize())
                                                            .contentType(att.getContentType())
                                                            .uploadedAt(att.getUploadedAt())
                                                            .createdAt(att.getCreatedAt())
                                                            .updatedAt(att.getUpdatedAt())
                                                            .build())
                                                    .collect(Collectors.toList())
                                            : List.of();

                                    return DeliveryOperationalItemResponse.builder()
                                            .id(item.getId())
                                            .deliveryId(entity.getId())
                                            .title(item.getTitle())
                                            .description(item.getDescription())
                                            .status(item.getStatus().name())
                                            .startedAt(item.getStartedAt())
                                            .finishedAt(item.getFinishedAt())
                                            .attachments(attachments)
                                            .createdAt(item.getCreatedAt())
                                            .updatedAt(item.getUpdatedAt())
                                            .build();
                                })
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public static Delivery toEntity(DeliveryRequest dto, Task task) {
        if (dto == null) return null;

        Delivery delivery = Delivery.builder()
                .task(task)
                .flowType(task != null ? task.getFlowType() : null)
                .environment(dto.getEnvironment() != null ? Environment.fromString(dto.getEnvironment()) : (task != null ? task.getEnvironment() : null))
                .status(dto.getStatus() != null ? DeliveryStatus.fromString(dto.getStatus()) : DeliveryStatus.PENDING)
                .notes(dto.getNotes())
                .build();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            dto.getItems().forEach(itemDto -> {
                var item = DeliveryItemAdapter.toEntity(itemDto, delivery, null);
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

        if (dto.getEnvironment() != null) {
            entity.setEnvironment(Environment.fromString(dto.getEnvironment()));
        }

        entity.setNotes(dto.getNotes());
    }
}
