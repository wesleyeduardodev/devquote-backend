package br.com.devquote.adapter;
import br.com.devquote.dto.request.RequesterRequest;
import br.com.devquote.dto.response.RequesterResponse;
import br.com.devquote.entity.Requester;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequesterAdapter {

    public static RequesterResponse toResponseDTO(Requester entity) {
        if (entity == null) {
            return null;
        }

        return RequesterResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Requester toEntity(RequesterRequest dto) {
        if (dto == null) {
            return null;
        }

        return Requester.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }

    public static void updateEntityFromDto(RequesterRequest dto, Requester entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
    }
}
