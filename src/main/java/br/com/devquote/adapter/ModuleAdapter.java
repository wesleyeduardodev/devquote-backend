package br.com.devquote.adapter;
import br.com.devquote.dto.request.ModuleRequest;
import br.com.devquote.dto.response.ModuleResponse;
import br.com.devquote.entity.SystemModule;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ModuleAdapter {

    public static ModuleResponse toResponseDTO(SystemModule entity) {
        if (entity == null) {
            return null;
        }
        return ModuleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SystemModule toEntity(ModuleRequest dto) {
        if (dto == null) {
            return null;
        }
        return SystemModule.builder()
                .name(dto.getName())
                .build();
    }

    public static void updateEntityFromDto(ModuleRequest dto, SystemModule entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.getName());
    }
}
