package br.com.devquote.entity;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification_config", uniqueConstraints = {
    @UniqueConstraint(name = "uk_notification_config_type", columnNames = {"config_type", "notification_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationConfig extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private NotificationConfigType configType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "primary_email")
    private String primaryEmail;

    @Column(name = "copy_emails", columnDefinition = "TEXT")
    private String copyEmails;

    @Column(name = "phone_numbers", columnDefinition = "TEXT")
    private String phoneNumbers;

    public List<String> getCopyEmailsList() {
        if (copyEmails == null || copyEmails.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(copyEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }

    public void setCopyEmailsList(List<String> emails) {
        this.copyEmails = emails != null ? String.join(",", emails) : null;
    }

    public List<String> getPhoneNumbersList() {
        if (phoneNumbers == null || phoneNumbers.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(phoneNumbers.split(","))
                .map(String::trim)
                .filter(phone -> !phone.isEmpty())
                .toList();
    }

    public void setPhoneNumbersList(List<String> phones) {
        this.phoneNumbers = phones != null ? String.join(",", phones) : null;
    }
}