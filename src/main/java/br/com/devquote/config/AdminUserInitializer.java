package br.com.devquote.config;

import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.entity.UserProfile;
import br.com.devquote.enums.ProfileType;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserProfileRepository;
import br.com.devquote.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeAdminUser() {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        try {
            // Verificar se usuário admin já existe
            Optional<User> existingAdmin = userRepository.findByUsername("admin");
            if (existingAdmin.isPresent()) {
                log.info("Usuário admin já existe: {}", existingAdmin.get().getUsername());
                ensureAdminProfile(existingAdmin.get());
                return;
            }

            // Criar usuário admin
            User adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@devquote.com")
                .name("Administrador")
                .active(true)
                .build();

            adminUser = userRepository.save(adminUser);
            log.info("Usuário admin criado com sucesso: {}", adminUser.getUsername());

            // Garantir perfil ADMIN
            ensureAdminProfile(adminUser);

        } catch (Exception e) {
            log.error("Erro ao criar usuário admin padrão", e);
        }
    }

    private void ensureAdminProfile(User adminUser) {
        try {
            // Buscar perfil ADMIN
            Optional<Profile> adminProfile = profileRepository.findByCode(ProfileType.ADMIN.getCode());
            if (adminProfile.isEmpty()) {
                log.warn("Perfil ADMIN não encontrado. Aguardando inicialização do PermissionService.");
                return;
            }

            // Verificar se usuário já tem o perfil ADMIN
            boolean hasAdminProfile = userProfileRepository.existsActiveByUserIdAndProfileId(
                adminUser.getId(), adminProfile.get().getId()
            );

            if (!hasAdminProfile) {
                // Criar vínculo usuário-perfil
                UserProfile userProfile = UserProfile.builder()
                    .user(adminUser)
                    .profile(adminProfile.get())
                    .active(true)
                    .build();

                userProfileRepository.save(userProfile);
                log.info("Perfil ADMIN vinculado ao usuário admin com sucesso");
            } else {
                log.info("Usuário admin já possui perfil ADMIN");
            }

        } catch (Exception e) {
            log.error("Erro ao vincular perfil ADMIN ao usuário admin", e);
        }
    }
}