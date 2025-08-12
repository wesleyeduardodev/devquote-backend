package br.com.devquote.configuration.security;
import br.com.devquote.entity.Role;
import br.com.devquote.entity.User;
import br.com.devquote.repository.RoleRepository;
import br.com.devquote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AuthTestUserSeeder {

    private static final Logger log = LoggerFactory.getLogger(AuthTestUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            upsertUser(
                    "admin@devquote.com",
                    "admin@devquote.com",
                    "admin123",
                    "ADMIN",
                    "System",
                    "Administrator"
            );

            upsertUser(
                    "dev@devquote.com",
                    "dev@devquote.com",
                    "dev123",
                    "DEVELOPER",
                    "John",
                    "Developer"
            );

            upsertUser(
                    "user@devquote.com",
                    "user@devquote.com",
                    "user123",
                    "USER",
                    "Jane",
                    "User"
            );
        };
    }

    private void upsertUser(String email,
                            String username,
                            String rawPassword,
                            String roleName,
                            String firstName,
                            String lastName) {

        Role role = roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " não encontrada. Garanta que as roles/permissions foram inseridas."));

        var opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            User user = User.builder()
                    .username(username)
                    .email(email.toLowerCase(Locale.ROOT))
                    .password(passwordEncoder.encode(rawPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(Set.of(role))
                    .build();
            userRepository.save(user);
            log.info("Usuário criado: {} (role: {}) / senha: {}", email, roleName, rawPassword);
        } else {
            User user = opt.get();
            user.setUsername(username);
            user.setEmail(email.toLowerCase(Locale.ROOT));
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setRoles(Set.of(role));
            userRepository.save(user);
            log.info("Usuário atualizado: {} (role: {}) / senha: {}", email, roleName, rawPassword);
        }
    }
}
