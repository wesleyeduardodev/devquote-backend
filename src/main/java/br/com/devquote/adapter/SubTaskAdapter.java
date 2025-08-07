package br.com.devquote.adapter;
import br.com.devquote.dto.request.SubTaskRequestDTO;
import br.com.devquote.dto.request.SubTaskUpdateRequestDTO;
import br.com.devquote.dto.response.SubTaskResponseDTO;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class SubTaskAdapter {

    public static SubTaskResponseDTO toResponseDTO(SubTask entity) {
        if (entity == null) {
            return null;
        }

        return SubTaskResponseDTO.builder()
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

    public static SubTask toEntity(SubTaskRequestDTO dto, Task task) {
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

    public static void updateEntityFromDto(SubTaskRequestDTO dto, SubTask entity, Task task) {
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

    public static void updateEntityFromDto(SubTaskUpdateRequestDTO dto, SubTask entity, Task task) {
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

    public static List<SubTaskResponseDTO> toResponseDTOList(List<SubTask> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(SubTaskAdapter::toResponseDTO)
                .toList();
    }
}
