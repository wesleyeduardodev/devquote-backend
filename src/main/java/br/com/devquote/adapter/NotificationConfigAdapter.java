package br.com.devquote.adapter;

import br.com.devquote.dto.request.NotificationConfigRequest;
import br.com.devquote.dto.response.NotificationConfigResponse;
import br.com.devquote.entity.NotificationConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NotificationConfigAdapter {

    public static NotificationConfigResponse toResponseDTO(NotificationConfig entity) {
        if (entity == null) {
            return null;
        }

        return NotificationConfigResponse.builder()
                .id(entity.getId())
                .configType(entity.getConfigType())
                .notificationType(entity.getNotificationType())
                .primaryEmail(entity.getPrimaryEmail())
                .copyEmails(entity.getCopyEmailsList())
                .phoneNumbers(entity.getPhoneNumbersList())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static NotificationConfig toEntity(NotificationConfigRequest dto) {
        if (dto == null) {
            return null;
        }

        NotificationConfig entity = NotificationConfig.builder()
                .configType(dto.getConfigType())
                .notificationType(dto.getNotificationType())
                .primaryEmail(dto.getPrimaryEmail())
                .build();

        entity.setCopyEmailsList(dto.getCopyEmails());
        entity.setPhoneNumbersList(dto.getPhoneNumbers());

        return entity;
    }

    public static void updateEntityFromDto(NotificationConfigRequest dto, NotificationConfig entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setConfigType(dto.getConfigType());
        entity.setNotificationType(dto.getNotificationType());
        entity.setPrimaryEmail(dto.getPrimaryEmail());
        entity.setCopyEmailsList(dto.getCopyEmails());
        entity.setPhoneNumbersList(dto.getPhoneNumbers());
    }
}