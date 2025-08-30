package br.com.devquote.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "devquote.notification.email")
public class EmailProperties {
    private boolean enabled = true;
    private String from = "wesleyeduardo.dev@gmail.com";
}