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
                .useRequesterContact(entity.getUseRequesterContact())
                .primaryEmail(entity.getPrimaryEmail())
                .primaryPhone(entity.getPrimaryPhone())
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
                .useRequesterContact(dto.getUseRequesterContact())
                .primaryEmail(Boolean.TRUE.equals(dto.getUseRequesterContact()) ? null : dto.getPrimaryEmail())
                .primaryPhone(Boolean.TRUE.equals(dto.getUseRequesterContact()) ? null : dto.getPrimaryPhone())
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
        entity.setUseRequesterContact(dto.getUseRequesterContact());

        // Se usar contato do solicitante, limpar campos de contato da configuração
        if (Boolean.TRUE.equals(dto.getUseRequesterContact())) {
            entity.setPrimaryEmail(null);
            entity.setPrimaryPhone(null);
        } else {
            entity.setPrimaryEmail(dto.getPrimaryEmail());
            entity.setPrimaryPhone(dto.getPrimaryPhone());
        }

        entity.setCopyEmailsList(dto.getCopyEmails());
        entity.setPhoneNumbersList(dto.getPhoneNumbers());
    }
}