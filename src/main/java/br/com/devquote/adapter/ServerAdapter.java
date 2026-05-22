package br.com.devquote.adapter;
import br.com.devquote.dto.request.ServerRequest;
import br.com.devquote.dto.response.ServerResponse;
import br.com.devquote.entity.Server;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerAdapter {

    public static ServerResponse toResponseDTO(Server entity) {
        if (entity == null) {
            return null;
        }
        return ServerResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .link(entity.getLink())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Server toEntity(ServerRequest dto) {
        if (dto == null) {
            return null;
        }
        return Server.builder()
                .name(dto.getName())
                .link(dto.getLink())
                .build();
    }

    public static void updateEntityFromDto(ServerRequest dto, Server entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.getName());
        entity.setLink(dto.getLink());
    }
}
