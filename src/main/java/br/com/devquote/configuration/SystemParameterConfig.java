package br.com.devquote.configuration;

import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemParameterConfig {

    private final SystemParameterService systemParameterService;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        try {
            String host = systemParameterService.getString("MAIL_HOST");
            Integer port = systemParameterService.getInteger("MAIL_PORT");
            String username = systemParameterService.getString("MAIL_USERNAME");
            String password = systemParameterService.getString("MAIL_PASSWORD");

            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", systemParameterService.getBoolean("MAIL_SMTP_AUTH", true));
            props.put("mail.smtp.starttls.enable", systemParameterService.getBoolean("MAIL_SMTP_STARTTLS_ENABLE", true));
            props.put("mail.smtp.ssl.trust", systemParameterService.getString("MAIL_SMTP_SSL_TRUST", "smtp.gmail.com"));
            props.put("mail.debug", "false");

            log.info("JavaMailSender configurado com sucesso via SystemParameter: host={}, port={}, username={}", host, port, username);
        } catch (Exception e) {
            log.error("Erro ao configurar JavaMailSender via SystemParameter. Usando configuração padrão do application.yml", e);
        }

        return mailSender;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                try {
                    List<String> allowedOrigins = systemParameterService.getList("DEVQUOTE_CORS_ALLOWED_ORIGINS");

                    registry.addMapping("/api/**")
                            .allowedOrigins(allowedOrigins.toArray(new String[0]))
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                            .allowedHeaders("*")
                            .allowCredentials(true)
                            .maxAge(3600);

                    log.info("CORS configurado com sucesso via SystemParameter: allowedOrigins={}", allowedOrigins);
                } catch (Exception e) {
                    log.error("Erro ao configurar CORS via SystemParameter. Usando configuração padrão do application.yml", e);
                }
            }
        };
    }
}
