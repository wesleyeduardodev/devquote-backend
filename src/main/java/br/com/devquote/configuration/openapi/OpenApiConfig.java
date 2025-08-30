package br.com.devquote.configuration.openapi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("DevQuote API")
                        .description("Sistema de orçamentos automatizados.\n\n" +
                                "📅 Data da subida: 30/08/2025 - 18:00:00\n" +
                                "🔖 Versão: 1.0.2\n" +
                                "👨‍💻 Autor: Wesley Eduardo\n")
                        .version("1.0.0")
                        .termsOfService("https://www.devquote.com.br/terms")
                        .contact(new Contact()
                                .name("Suporte DevQuote")
                                .email("wesleyeduardo.dev@gmail.com")
                                .url("https://www.devquote.com.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
