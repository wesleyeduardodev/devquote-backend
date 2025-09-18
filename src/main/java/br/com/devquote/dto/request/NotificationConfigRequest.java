package br.com.devquote.dto.request;

import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationConfigRequest {

    @NotNull(message = "Config type is required")
    private NotificationConfigType configType;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @Email(message = "Invalid email format")
    private String primaryEmail;

    private List<String> copyEmails;

    private List<String> phoneNumbers;
}