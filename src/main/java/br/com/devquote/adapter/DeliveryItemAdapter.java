package br.com.devquote.adapter;
import br.com.devquote.dto.request.DeliveryItemRequest;
import br.com.devquote.dto.response.DeliveryItemResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.entity.Project;
import br.com.devquote.enums.DeliveryStatus;
import lombok.experimental.UtilityClass;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public final class DeliveryItemAdapter {

    public static DeliveryItemResponse toResponseDTO(DeliveryItem entity) {
        if (entity == null) return null;

        return DeliveryItemResponse.builder()
                .id(entity.getId())
                .deliveryId(entity.getDelivery() != null ? entity.getDelivery().getId() : null)
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProject() != null ? entity.getProject().getName() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .branch(entity.getBranch())
                .sourceBranch(entity.getSourceBranch())
                .pullRequest(entity.getPullRequest())
                .notes(entity.getNotes())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .taskId(entity.getDelivery() != null && entity.getDelivery().getTask() != null ?
                        entity.getDelivery().getTask().getId() : null)
                .taskName(entity.getDelivery() != null && entity.getDelivery().getTask() != null ?
                        entity.getDelivery().getTask().getTitle() : null)
                .taskCode(entity.getDelivery() != null && entity.getDelivery().getTask() != null ?
                        entity.getDelivery().getTask().getCode() : null)
                .merged(entity.getMerged())
                .mergedAt(entity.getMergedAt())
                .build();
    }

    public static List<DeliveryItemResponse> toResponseDTOList(List<DeliveryItem> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(DeliveryItemAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    public static DeliveryItem toEntity(DeliveryItemRequest dto, Delivery delivery, Project project) {
        if (dto == null) return null;

        return DeliveryItem.builder()
                .delivery(delivery)
                .project(project)
                .status(dto.getStatus() != null ? DeliveryStatus.fromString(dto.getStatus()) : DeliveryStatus.PENDING)
                .branch(dto.getBranch())
                .sourceBranch(dto.getSourceBranch())
                .pullRequest(dto.getPullRequest())
                .notes(dto.getNotes())
                .startedAt(dto.getStartedAt() != null ? dto.getStartedAt() : LocalDateTime.now())
                .finishedAt(dto.getFinishedAt())
                .build();
    }

    public static void updateEntityFromDto(DeliveryItemRequest dto, DeliveryItem entity, Delivery delivery, Project project) {
        if (dto == null || entity == null) return;

        if (delivery != null) entity.setDelivery(delivery);
        if (project != null) entity.setProject(project);

        entity.setStatus(dto.getStatus() != null ? DeliveryStatus.fromString(dto.getStatus()) : DeliveryStatus.PENDING);
        entity.setBranch(dto.getBranch());
        entity.setSourceBranch(dto.getSourceBranch());
        entity.setPullRequest(dto.getPullRequest());
        entity.setNotes(dto.getNotes());
        entity.setStartedAt(dto.getStartedAt());
        entity.setFinishedAt(dto.getFinishedAt());
    }
}