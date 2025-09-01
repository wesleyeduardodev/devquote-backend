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
                .code(entity.getCode())
                .link(entity.getLink())
                .meetingLink(entity.getMeetingLink())
                .notes(entity.getNotes())
                .hasSubTasks(entity.getHasSubTasks())
                .amount(entity.getAmount())
                .taskType(entity.getTaskType())
                .serverOrigin(entity.getServerOrigin())
                .systemModule(entity.getSystemModule())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdByUserId(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByUserName(entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null)
                .updatedByUserId(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getId() : null)
                .updatedByUserName(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getName() : null)
                .financialEmailSent(entity.getFinancialEmailSent())
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
                .code(dto.getCode())
                .link(dto.getLink())
                .meetingLink(dto.getMeetingLink())
                .notes(dto.getNotes())
                .hasSubTasks(dto.getHasSubTasks() != null ? dto.getHasSubTasks() : false)
                .amount(dto.getAmount())
                .taskType(dto.getTaskType())
                .serverOrigin(dto.getServerOrigin())
                .systemModule(dto.getSystemModule())
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
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
        entity.setCode(dto.getCode());
        entity.setLink(dto.getLink());
        entity.setMeetingLink(dto.getMeetingLink());
        entity.setNotes(dto.getNotes());
        entity.setHasSubTasks(dto.getHasSubTasks() != null ? dto.getHasSubTasks() : false);
        entity.setAmount(dto.getAmount());
        entity.setTaskType(dto.getTaskType());
        entity.setServerOrigin(dto.getServerOrigin());
        entity.setSystemModule(dto.getSystemModule());
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
    }
}
