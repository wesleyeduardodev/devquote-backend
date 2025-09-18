package br.com.devquote.dto.response;

import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationConfigResponse {

    private Long id;

    private NotificationConfigType configType;

    private NotificationType notificationType;

    private Boolean useRequesterContact;

    private String primaryEmail;

    private String primaryPhone;

    private List<String> copyEmails;

    private List<String> phoneNumbers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}