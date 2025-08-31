package br.com.devquote.adapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DeliveryAdapter {

    public static DeliveryResponse toResponseDTO(Delivery entity) {
        if (entity == null) return null;

        return DeliveryResponse.builder()
                .id(entity.getId())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskName(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .taskCode(entity.getTask() != null ? entity.getTask().getCode() : null)
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProject() != null ? entity.getProject().getName() : null)
                .branch(entity.getBranch())
                .pullRequest(entity.getPullRequest())
                .script(entity.getScript())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .sourceBranch(entity.getSourceBranch())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Delivery toEntity(DeliveryRequest dto, Task task, Project project) {
        if (dto == null) return null;

        return Delivery.builder()
                .task(task)
                .project(project)
                .branch(dto.getBranch())
                .pullRequest(dto.getPullRequest())
                .script(dto.getScript())
                .status(dto.getStatus())
                .notes(dto.getNotes())
                .sourceBranch(dto.getSourceBranch())
                .startedAt(dto.getStartedAt())
                .finishedAt(dto.getFinishedAt())
                .build();
    }

    public static void updateEntityFromDto(DeliveryRequest dto, Delivery entity, Task task, Project project) {
        if (dto == null || entity == null) return;

        if (task != null) entity.setTask(task);
        if (project != null) entity.setProject(project);

        entity.setBranch(dto.getBranch());
        entity.setPullRequest(dto.getPullRequest());
        entity.setScript(dto.getScript());
        entity.setStatus(dto.getStatus());
        entity.setStartedAt(dto.getStartedAt());
        entity.setFinishedAt(dto.getFinishedAt());
        entity.setNotes(dto.getNotes());
        entity.setSourceBranch(dto.getSourceBranch());
    }
}
