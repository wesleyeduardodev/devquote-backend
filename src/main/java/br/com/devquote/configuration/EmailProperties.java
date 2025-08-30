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
    
    @PostConstruct
    public void logConfiguration() {
        if (enabled) {
            log.info("=== EMAIL CONFIGURATION ===");
            log.info("Email notifications: ENABLED");
            log.info("Email from address: {}", from != null ? from : "NOT CONFIGURED");
            if (from == null || from.trim().isEmpty()) {
                log.error("EMAIL FROM ADDRESS IS NOT CONFIGURED! Set DEVQUOTE_EMAIL_FROM environment variable.");
            }
            log.info("==========================");
        } else {
            log.warn("=== EMAIL CONFIGURATION ===");
            log.warn("Email notifications: DISABLED");
            log.warn("Reason: DEVQUOTE_EMAIL_ENABLED is set to false or not configured");
            log.warn("To enable emails, set DEVQUOTE_EMAIL_ENABLED=true in environment variables");
            log.warn("==========================");
        }
    }
}