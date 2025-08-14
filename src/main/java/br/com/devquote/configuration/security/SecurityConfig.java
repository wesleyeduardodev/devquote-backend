package br.com.devquote.configuration.security;
import br.com.devquote.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter authTokenFilter;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("#{'${devquote.cors.allowed-origins:}'.split(',')}")
    private List<String> allowedOriginsFromYaml;

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // públicos
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // tudo de dashboard/projetos/tarefas/orçamentos/entregas/solicitantes: só precisa estar logado
                        .requestMatchers("/api/dashboard/**").authenticated()
                        .requestMatchers("/api/tasks/**").authenticated()
                        .requestMatchers("/api/projects/**").authenticated()
                        .requestMatchers("/api/quotes/**").authenticated()
                        .requestMatchers("/api/deliveries/**").authenticated()
                        .requestMatchers("/api/requesters/**").authenticated()

                        // admin continua restrito
                        .requestMatchers("/api/admin/**").hasAuthority("admin:users")

                        // demais: autenticado
                        .anyRequest().authenticated()
                )


                //.authorizeHttpRequests(auth -> auth
                // Públicos
                // .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                //  .requestMatchers("/h2-console/**").permitAll()
                //  .requestMatchers("/.well-known/**").permitAll()
                // .requestMatchers("/oauth2/**").permitAll()
                // .requestMatchers("/login").permitAll()
                // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Dashboard (exemplo de screen-level)
                // .requestMatchers("/api/dashboard/**").hasAuthority("dashboard:view")

                // ===== TASKS =====
                // fluxo completo com subtarefas
                //  .requestMatchers(HttpMethod.POST,   "/api/tasks/full").hasAnyAuthority("tasks:create","admin:users")
                // .requestMatchers(HttpMethod.PUT,    "/api/tasks/full/**").hasAnyAuthority("tasks:edit","admin:users")
                // .requestMatchers(HttpMethod.DELETE, "/api/tasks/full/**").hasAnyAuthority("tasks:delete","admin:users")
                // CRUD simples
                //  .requestMatchers("/api/tasks/create").hasAnyAuthority("tasks:create","admin:users")
                //  .requestMatchers("/api/tasks/*/edit").hasAnyAuthority("tasks:edit","admin:users")
                //  .requestMatchers("/api/tasks/*/delete").hasAnyAuthority("tasks:delete","admin:users")
                // .requestMatchers("/api/tasks/**").hasAnyAuthority("tasks:view","admin:users")

                // ===== REQUESTERS =====
                // .requestMatchers(HttpMethod.POST,   "/api/requesters/**").hasAnyAuthority("admin:users")
                // .requestMatchers(HttpMethod.PUT,    "/api/requesters/**").hasAnyAuthority("admin:users")
                // .requestMatchers(HttpMethod.DELETE, "/api/requesters/**").hasAnyAuthority("admin:users")
                // .requestMatchers("/api/requesters/**").authenticated()

                // ===== PROJECTS =====
                //  .requestMatchers("/api/projects/create").hasAnyAuthority("projects:create","admin:users")
                // .requestMatchers("/api/projects/*/edit").hasAnyAuthority("projects:edit","admin:users")
                //  .requestMatchers("/api/projects/*/delete").hasAnyAuthority("projects:delete","admin:users")
                //  .requestMatchers("/api/projects/**").hasAnyAuthority("projects:view","admin:users")

                // ===== QUOTES =====
                //   .requestMatchers("/api/quotes/create").hasAnyAuthority("quotes:create","admin:users")
                // .requestMatchers("/api/quotes/*/edit").hasAnyAuthority("quotes:edit","admin:users")
                // .requestMatchers("/api/quotes/**").hasAnyAuthority("quotes:view","admin:users")

                // ===== DELIVERIES =====
                // .requestMatchers("/api/deliveries/**").hasAnyAuthority("deliveries:view","admin:users")

                // ===== ADMIN =====
                // .requestMatchers("/api/admin/**").hasAnyAuthority("admin:users")

                // Demais rotas: logado
                // .anyRequest().authenticated()

                //)
                .formLogin(form -> form.loginPage("/login").permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(hs256Decoder()))
                )
                .userDetailsService(userDetailsService)
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private JwtDecoder hs256Decoder() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = new ArrayList<>();
        if (allowedOriginsFromYaml != null) {
            for (String o : allowedOriginsFromYaml) {
                if (o != null && !o.isBlank()) {
                    origins.add(o.trim());
                }
            }
        }
        if (origins.isEmpty()) {
            // Fallback seguro para dev
            origins.add("http://localhost:5173");
            origins.add("http://localhost:3000");
            origins.add("http://localhost:8080");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins); // use Origin exatas quando allowCredentials=true
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
