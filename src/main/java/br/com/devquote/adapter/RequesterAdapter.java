package br.com.devquote.adapter;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import br.com.devquote.entity.Requester;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequesterAdapter {

    public static RequesterResponseDTO toResponseDTO(Requester entity) {
        if (entity == null) {
            return null;
        }

        return RequesterResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Requester toEntity(RequesterRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Requester.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }

    public static void updateEntityFromDto(RequesterRequestDTO dto, Requester entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
    }
}
