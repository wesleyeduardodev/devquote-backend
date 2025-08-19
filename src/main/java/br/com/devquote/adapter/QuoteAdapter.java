package br.com.devquote.adapter;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import br.com.devquote.entity.Quote;
import br.com.devquote.entity.Task;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuoteAdapter {

    public static QuoteResponseDTO toResponseDTO(Quote entity) {
        if (entity == null) {
            return null;
        }

        return QuoteResponseDTO.builder()
                .id(entity.getId())
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskName(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .taskCode(entity.getTask() != null ? entity.getTask().getCode() : null)
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Quote toEntity(QuoteRequestDTO dto, Task task) {
        if (dto == null) {
            return null;
        }

        return Quote.builder()
                .task(task)
                .status(dto.getStatus())
                .totalAmount(dto.getTotalAmount())
                .build();
    }

    public static void updateEntityFromDto(QuoteRequestDTO dto, Quote entity, Task task) {
        if (dto == null || entity == null) {
            return;
        }

        if (task != null) {
            entity.setTask(task);
        }

        entity.setStatus(dto.getStatus());
        entity.setTotalAmount(dto.getTotalAmount());
    }
}
