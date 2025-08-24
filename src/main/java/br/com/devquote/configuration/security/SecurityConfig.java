package br.com.devquote.configuration.security;
import org.springframework.security.core.userdetails.UserDetailsService;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/public/**",
            "/.well-known/**",
            "/oauth2/**",
            "/error",             // evita loop em erros
            "/actuator/health"    // opcional
    };

    private final UserDetailsService userDetailsService;
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

                // Não queremos formLogin ou basic — evita redirecionar para /login
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Swagger público
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // Endpoints públicos (login/register/refresh etc.)
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // H2 console (apenas para DEV)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Pré‑flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Regras de domínio da aplicação — autenticado
                        .requestMatchers("/api/dashboard/**").authenticated()
                        .requestMatchers("/api/tasks/**").authenticated()
                        .requestMatchers("/api/projects/**").authenticated()
                        .requestMatchers("/api/quotes/**").authenticated()
                        .requestMatchers("/api/deliveries/**").authenticated()
                        .requestMatchers("/api/requesters/**").authenticated()

                        // Admin com perfil específico
                        .requestMatchers("/api/admin/**").hasAuthority("PROFILE_ADMIN")

                        // Demais rotas: autenticado
                        .anyRequest().authenticated()
                )

                // Resource Server com JWT (HS256)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(hs256Decoder()))
                )

                // H2 precisa disso para exibir frames
                .headers(h -> h.frameOptions(f -> f.disable()))

                // UserDetails + Filtro custom de JWT (se aplicável ao seu fluxo)
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
        configuration.setAllowedOrigins(origins); // Use origins exatas quando allowCredentials=true
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
