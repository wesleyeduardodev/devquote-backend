package br.com.devquote.adapter;
import br.com.devquote.dto.request.SubTaskRequest;
import br.com.devquote.dto.request.SubTaskUpdateRequest;
import br.com.devquote.dto.response.SubTaskResponse;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class SubTaskAdapter {

    public static SubTaskResponse toResponseDTO(SubTask entity) {
        if (entity == null) {
            return null;
        }

        return SubTaskResponse.builder()
                .id(entity.getId())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SubTask toEntity(SubTaskRequest dto, Task task) {
        if (dto == null) {
            return null;
        }

        return SubTask.builder()
                .task(task)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .build();
    }

    public static void updateEntityFromDto(SubTaskRequest dto, SubTask entity, Task task) {
        if (dto == null || entity == null) {
            return;
        }

        if (task != null) {
            entity.setTask(task);
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setStatus(dto.getStatus());
    }

    public static void updateEntityFromDto(SubTaskUpdateRequest dto, SubTask entity, Task task) {
        if (dto == null || entity == null) {
            return;
        }

        if (task != null) {
            entity.setTask(task);
        }

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setStatus(dto.getStatus());
    }

    public static List<SubTaskResponse> toResponseDTOList(List<SubTask> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(SubTaskAdapter::toResponseDTO)
                .toList();
    }

    public static SubTask toEntity(SubTaskUpdateRequest dto, Task task) {
        if (dto == null) {
            return null;
        }

        return SubTask.builder()
                .task(task)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .build();
    }
}
