package br.com.devquote.adapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Quote;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DeliveryAdapter {

    public static DeliveryResponse toResponseDTO(Delivery entity) {
        if (entity == null) return null;

        return DeliveryResponse.builder()
                .id(entity.getId())
                .quoteId(entity.getQuote() != null && entity.getQuote().getId() != null ? entity.getQuote().getId() : null)
                .taskId(entity.getQuote() != null && entity.getQuote().getTask() != null ? entity.getQuote().getTask().getId() : null)
                .taskName(entity.getQuote() != null && entity.getQuote().getTask() != null ? entity.getQuote().getTask().getDescription() : null)
                .taskCode(entity.getQuote() != null && entity.getQuote().getTask() != null ? entity.getQuote().getTask().getCode() : null)
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

    public static Delivery toEntity(DeliveryRequest dto, Quote quote, Project project) {
        if (dto == null) return null;

        return Delivery.builder()
                .quote(quote)
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

    public static void updateEntityFromDto(DeliveryRequest dto, Delivery entity, Quote quote, Project project) {
        if (dto == null || entity == null) return;

        if (quote != null) entity.setQuote(quote);
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
