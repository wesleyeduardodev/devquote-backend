package br.com.devquote.configuration.security;

import br.com.devquote.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter authTokenFilter;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()

                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login").permitAll()

                        // Screen-based authorization
                        .requestMatchers("/api/dashboard/**").hasAuthority("SCOPE_dashboard:view")

                        .requestMatchers("/api/projects/create").hasAnyAuthority("SCOPE_projects:create", "SCOPE_admin:users")
                        .requestMatchers("/api/projects/*/edit").hasAnyAuthority("SCOPE_projects:edit", "SCOPE_admin:users")
                        .requestMatchers("/api/projects/*/delete").hasAnyAuthority("SCOPE_projects:delete", "SCOPE_admin:users")
                        .requestMatchers("/api/projects/**").hasAnyAuthority("SCOPE_projects:view", "SCOPE_admin:users")

                        .requestMatchers("/api/tasks/create").hasAnyAuthority("SCOPE_tasks:create", "SCOPE_admin:users")
                        .requestMatchers("/api/tasks/*/edit").hasAnyAuthority("SCOPE_tasks:edit", "SCOPE_admin:users")
                        .requestMatchers("/api/tasks/*/delete").hasAnyAuthority("SCOPE_tasks:delete", "SCOPE_admin:users")
                        .requestMatchers("/api/tasks/**").hasAnyAuthority("SCOPE_tasks:view", "SCOPE_admin:users")

                        .requestMatchers("/api/quotes/create").hasAnyAuthority("SCOPE_quotes:create", "SCOPE_admin:users")
                        .requestMatchers("/api/quotes/*/edit").hasAnyAuthority("SCOPE_quotes:edit", "SCOPE_admin:users")
                        .requestMatchers("/api/quotes/**").hasAnyAuthority("SCOPE_quotes:view", "SCOPE_admin:users")

                        .requestMatchers("/api/deliveries/**").hasAnyAuthority("SCOPE_deliveries:view", "SCOPE_admin:users")

                        .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin:users")

                        // Default: require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(hs256Decoder())) // <<< usa HS256 aqui
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
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}