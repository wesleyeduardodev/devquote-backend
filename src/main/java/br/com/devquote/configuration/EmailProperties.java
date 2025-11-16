package br.com.devquote.configuration;
import br.com.devquote.service.SystemParameterService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Slf4j
@Data
@Component
@RequiredArgsConstructor
public class EmailProperties {

    private final SystemParameterService systemParameterService;

    private boolean enabled;
    private String from;

    @PostConstruct
    public void loadFromSystemParameters() {
        try {
            this.enabled = systemParameterService.getBoolean("DEVQUOTE_EMAIL_ENABLED", false);
            this.from = systemParameterService.getString("DEVQUOTE_EMAIL_FROM", "noreply@devquote.com.br");

            if (enabled) {
                log.info("Email notifications: ENABLED (loaded from system_parameter)");
                log.info("Email from address: {}", from);
            } else {
                log.info("Email notifications: DISABLED (loaded from system_parameter)");
            }
        } catch (Exception e) {
            log.error("Erro ao carregar configurações de email do system_parameter: {}", e.getMessage());
            log.warn("Usando valores padrão: enabled=false, from=noreply@devquote.com.br");
            this.enabled = false;
            this.from = "noreply@devquote.com.br";
        }
    }

    public boolean isEnabled() {
        return systemParameterService.getBoolean("DEVQUOTE_EMAIL_ENABLED", false);
    }

    public String getFrom() {
        return systemParameterService.getString("DEVQUOTE_EMAIL_FROM", "noreply@devquote.com.br");
    }
}