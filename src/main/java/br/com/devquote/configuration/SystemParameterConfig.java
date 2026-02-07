package br.com.devquote.configuration;
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

    private final CorsProperties corsProperties;
    private final MailProperties mailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        try {
            mailSender.setHost(mailProperties.getHost());
            mailSender.setPort(mailProperties.getPort());
            mailSender.setUsername(mailProperties.getUsername());
            mailSender.setPassword(mailProperties.getPassword());

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", mailProperties.getSmtp().getAuth());
            props.put("mail.smtp.starttls.enable", mailProperties.getSmtp().getStarttls());
            props.put("mail.smtp.ssl.trust", mailProperties.getSmtp().getSslTrust());
            props.put("mail.debug", "false");

            log.info("JavaMailSender configurado com sucesso: host={}, port={}, username={}",
                    mailProperties.getHost(), mailProperties.getPort(), mailProperties.getUsername());
        } catch (Exception e) {
            log.error("Erro ao configurar JavaMailSender", e);
        }

        return mailSender;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> allowedOrigins = corsProperties.getAllowedOrigins();

                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                log.info("CORS configurado via application.yml: allowedOrigins={}", allowedOrigins);
            }
        };
    }
}
