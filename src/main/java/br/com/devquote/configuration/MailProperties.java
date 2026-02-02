package br.com.devquote.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String from;
    private Smtp smtp = new Smtp();

    @Getter
    @Setter
    public static class Smtp {
        private Boolean auth = true;
        private Boolean starttls = true;
        private String sslTrust = "smtp.gmail.com";
    }
}
