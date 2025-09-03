package br.com.devquote.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "devquote.notification.email")
public class EmailProperties {
    private boolean enabled;
    private String from;
    private String financeEmail;
    
    @PostConstruct
    public void logConfiguration() {
        if (enabled) {
            log.debug("Email notifications: ENABLED");
            log.debug("Email from address: {}", from != null ? from : "NOT CONFIGURED");
            log.debug("Finance email address: {}", financeEmail != null ? financeEmail : "NOT CONFIGURED");
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