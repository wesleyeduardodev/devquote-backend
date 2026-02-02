package br.com.devquote.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integrations")
public class IntegrationsProperties {
    private GitHub github = new GitHub();
    private ClickUp clickup = new ClickUp();

    @Getter
    @Setter
    public static class GitHub {
        private Boolean enabled = false;
        private String token;
    }

    @Getter
    @Setter
    public static class ClickUp {
        private Boolean enabled = false;
        private String token;
    }
}
