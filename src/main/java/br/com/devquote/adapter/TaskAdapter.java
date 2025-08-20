package br.com.devquote.adapter;
import br.com.devquote.dto.request.TaskRequest;
import br.com.devquote.dto.response.TaskResponse;
import br.com.devquote.entity.Requester;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskAdapter {

    public static TaskResponse toResponseDTO(Task entity) {
        if (entity == null) {
            return null;
        }

        return TaskResponse.builder()
                .id(entity.getId())
                .requesterId(entity.getRequester() != null ? entity.getRequester().getId() : null)
                .requesterName(entity.getRequester() != null ? entity.getRequester().getName() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .code(entity.getCode())
                .link(entity.getLink())
                .meetingLink(entity.getMeetingLink())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Task toEntity(TaskRequest dto, Requester requester) {
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
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
                .build();
    }

    public static void updateEntityFromDto(TaskRequest dto, Task entity, Requester requester) {
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
        entity.setMeetingLink(dto.getMeetingLink());
        entity.setNotes(dto.getNotes());
    }
}
