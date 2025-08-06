package br.com.devquote.adapter;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.entity.Requester;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskAdapter {

    public static TaskResponseDTO toResponseDTO(Task entity) {
        if (entity == null) {
            return null;
        }

        return TaskResponseDTO.builder()
                .id(entity.getId())
                .requesterId(entity.getRequester() != null ? entity.getRequester().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .code(entity.getCode())
                .link(entity.getLink())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Task toEntity(TaskRequestDTO dto, Requester requester) {
        if (dto == null) {
            return null;
        }

        return Task.builder()
                .requester(requester)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .build();
    }

    public static void updateEntityFromDto(TaskRequestDTO dto, Task entity, Requester requester) {
        if (dto == null || entity == null) {
            return;
        }

        if (requester != null) {
            entity.setRequester(requester);
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setCode(dto.getCode());
        entity.setLink(dto.getLink());
    }
}
