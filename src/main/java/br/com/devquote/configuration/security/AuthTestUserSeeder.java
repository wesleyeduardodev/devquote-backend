package br.com.devquote.configuration.security;
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

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AuthTestUserSeeder {

    private static final Logger log = LoggerFactory.getLogger(AuthTestUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedTestUser() {
        return args -> {
            var email = "test@devquote.com";
            var username = "testuser";
            var rawPassword = "123456";

            var roleUser = roleRepository.findByNameWithPermissions("USER")
                    .orElseThrow(() -> new IllegalStateException("Role USER não encontrada. Rode os INSERTs de role/permission."));

            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                var user = User.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .firstName("Test")
                        .lastName("User")
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .roles(Set.of(roleUser))
                        .build();
                userRepository.save(user);
                log.info("Usuário de teste criado: {} / {}", email, rawPassword);
            } else {
                var user = userOpt.get();
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setRoles(Set.of(roleUser));
                userRepository.save(user);
                log.info("Usuário de teste atualizado: {} / {}", email, rawPassword);
            }
        };
    }
}
