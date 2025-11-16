package br.com.devquote.adapter;

import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.SystemParameterResponse;
import br.com.devquote.entity.SystemParameter;

import java.time.format.DateTimeFormatter;

public class SystemParameterAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static SystemParameterResponse toResponseDTO(SystemParameter entity) {
        if (entity == null) {
            return null;
        }

        return SystemParameterResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .value(entity.getValue())
                .description(entity.getDescription())
                .isEncrypted(entity.getIsEncrypted())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }

    public static SystemParameter toEntity(SystemParameterRequest dto) {
        if (dto == null) {
            return null;
        }

        return SystemParameter.builder()
                .name(dto.getName())
                .value(dto.getValue())
                .description(dto.getDescription())
                .isEncrypted(dto.getIsEncrypted() != null ? dto.getIsEncrypted() : false)
                .build();
    }

    public static void updateEntityFromDto(SystemParameterRequest dto, SystemParameter entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setValue(dto.getValue());
        entity.setDescription(dto.getDescription());
        entity.setIsEncrypted(dto.getIsEncrypted() != null ? dto.getIsEncrypted() : false);
    }
}
