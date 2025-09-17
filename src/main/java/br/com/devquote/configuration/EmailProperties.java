package br.com.devquote.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "devquote.notification.email")
public class EmailProperties {
    private boolean enabled;
    private String from;
    private String financeEmail;
    
    /**
     * Retorna lista de emails financeiros (separados por vírgula)
     */
    public List<String> getFinanceEmails() {
        if (financeEmail == null || financeEmail.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(financeEmail.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }

    /**
     * Retorna o primeiro email financeiro (destinatário principal)
     */
    public String getPrimaryFinanceEmail() {
        List<String> emails = getFinanceEmails();
        return emails.isEmpty() ? null : emails.get(0);
    }

    /**
     * Retorna emails financeiros secundários (para CC)
     */
    public List<String> getSecondaryFinanceEmails() {
        List<String> emails = getFinanceEmails();
        return emails.size() <= 1 ? List.of() : emails.subList(1, emails.size());
    }

    @PostConstruct
    public void logConfiguration() {
        if (enabled) {
            log.debug("Email notifications: ENABLED");
            log.debug("Email from address: {}", from != null ? from : "NOT CONFIGURED");
            log.debug("Finance email address: {}", financeEmail != null ? financeEmail : "NOT CONFIGURED");

            List<String> financeEmails = getFinanceEmails();
            if (!financeEmails.isEmpty()) {
                log.debug("Finance emails parsed: {} total emails", financeEmails.size());
                log.debug("Primary finance email: {}", getPrimaryFinanceEmail());
                if (!getSecondaryFinanceEmails().isEmpty()) {
                    log.debug("Secondary finance emails: {}", getSecondaryFinanceEmails());
                }
            }

            if (from == null || from.trim().isEmpty()) {
                log.error("EMAIL FROM ADDRESS IS NOT CONFIGURED! Set DEVQUOTE_EMAIL_FROM environment variable.");
            }
            if (financeEmail == null || financeEmail.trim().isEmpty()) {
                log.warn("FINANCE EMAIL ADDRESS IS NOT CONFIGURED! Set DEVQUOTE_EMAIL_FINANCE environment variable.");
            }
        } else {
            log.debug("Email notifications: DISABLED");
            log.debug("Reason: DEVQUOTE_EMAIL_ENABLED is set to false or not configured");
        }
    }
}