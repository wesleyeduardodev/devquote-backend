package br.com.devquote.adapter;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Quote;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DeliveryAdapter {

    public static DeliveryResponseDTO toResponseDTO(Delivery entity) {
        if (entity == null) return null;

        return DeliveryResponseDTO.builder()
                .id(entity.getId())
                .taskId(entity.getQuote() != null  && entity.getQuote().getTask() != null? entity.getQuote().getTask().getId() : null)
                .taskName(entity.getQuote() != null  && entity.getQuote().getTask() != null? entity.getQuote().getTask().getDescription() : null)
                .taskCode(entity.getQuote() != null  && entity.getQuote().getTask() != null? entity.getQuote().getTask().getCode() : null)
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProject() != null ? entity.getProject().getName() : null)
                .branch(entity.getBranch())
                .pullRequest(entity.getPullRequest())
                .script(entity.getScript())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Delivery toEntity(DeliveryRequestDTO dto, Quote quote, Project project) {
        if (dto == null) return null;

        return Delivery.builder()
                .quote(quote)
                .project(project)
                .branch(dto.getBranch())
                .pullRequest(dto.getPullRequest())
                .script(dto.getScript())
                .status(dto.getStatus())
                .startedAt(dto.getStartedAt())
                .finishedAt(dto.getFinishedAt())
                .build();
    }

    public static void updateEntityFromDto(DeliveryRequestDTO dto, Delivery entity, Quote quote, Project project) {
        if (dto == null || entity == null) return;

        if (quote != null) entity.setQuote(quote);
        if (project != null) entity.setProject(project);

        entity.setBranch(dto.getBranch());
        entity.setPullRequest(dto.getPullRequest());
        entity.setScript(dto.getScript());
        entity.setStatus(dto.getStatus());
        entity.setStartedAt(dto.getStartedAt());
        entity.setFinishedAt(dto.getFinishedAt());
    }
}
