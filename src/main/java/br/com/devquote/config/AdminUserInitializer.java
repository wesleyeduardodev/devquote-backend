package br.com.devquote.config;

import br.com.devquote.entity.*;
import br.com.devquote.enums.ProfileType;
import br.com.devquote.enums.ResourceType;
import br.com.devquote.enums.OperationType;
import br.com.devquote.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResourceRepository resourceRepository;
    private final OperationRepository operationRepository;
    private final ResourcePermissionRepository resourcePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1000) // Executa depois de outros listeners  
    public void initializeDefaultUsers() {
        log.info("=== INICIALIZANDO USUÁRIOS PADRÃO ===");
        try {
            // Criar usuários padrão
            createUserIfNotExists("admin", "admin@devquote.com", "Administrador", "admin123", ProfileType.ADMIN);
            createUserIfNotExists("manager", "manager@devquote.com", "Gerente", "admin123", ProfileType.MANAGER);
            createUserIfNotExists("user", "user@devquote.com", "Usuário", "admin123", ProfileType.USER);
            
            // Configurar permissões específicas
            configureUserPermissions();
            
            log.info("Usuários padrão inicializados com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao inicializar usuários padrão", e);
        }
    }

    private void createUserIfNotExists(String username, String email, String name, String password, ProfileType profileType) {
        try {
            // Verificar se usuário já existe
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent()) {
                log.info("Usuário {} já existe", username);
                ensureUserProfile(existingUser.get(), profileType);
                return;
            }

            // Criar usuário
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .name(name)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            user = userRepository.save(user);
            log.info("Usuário {} criado com sucesso: {}", username, user.getUsername());

            // Garantir perfil
            ensureUserProfile(user, profileType);

        } catch (Exception e) {
            log.error("Erro ao criar usuário {}", username, e);
        }
    }

    private void ensureUserProfile(User user, ProfileType profileType) {
        try {
            // Buscar perfil
            Optional<Profile> profile = profileRepository.findByCode(profileType.getCode());
            if (profile.isEmpty()) {
                log.warn("Perfil {} não encontrado para usuário {}", profileType.getCode(), user.getUsername());
                return;
            }

            // Verificar se usuário já tem o perfil
            boolean hasProfile = userProfileRepository.existsActiveByUserIdAndProfileId(
                    user.getId(), profile.get().getId()
            );

            if (!hasProfile) {
                // Criar vínculo usuário-perfil
                UserProfile userProfile = UserProfile.builder()
                        .user(user)
                        .profile(profile.get())
                        .active(true)
                        .build();

                userProfile = userProfileRepository.save(userProfile);
                log.info("Perfil {} vinculado ao usuário {} com sucesso", profileType.getCode(), user.getUsername());
            } else {
                log.info("Usuário {} já possui perfil {}", user.getUsername(), profileType.getCode());
            }

        } catch (Exception e) {
            log.error("Erro ao vincular perfil {} ao usuário {}", profileType.getCode(), user.getUsername(), e);
        }
    }

    private void configureUserPermissions() {
        try {
            log.info("Configurando permissões específicas dos usuários...");
            
            // Buscar perfis
            Profile adminProfile = profileRepository.findByCode(ProfileType.ADMIN.getCode()).orElse(null);
            Profile managerProfile = profileRepository.findByCode(ProfileType.MANAGER.getCode()).orElse(null);
            Profile userProfile = profileRepository.findByCode(ProfileType.USER.getCode()).orElse(null);
            
            if (adminProfile == null || managerProfile == null || userProfile == null) {
                log.warn("Alguns perfis não foram encontrados para configuração de permissões");
                return;
            }
            
            // Buscar recursos e operações
            List<Resource> allResources = resourceRepository.findAllOrderedByName();
            List<Operation> allOperations = operationRepository.findAllOrderedByName();
            
            if (allResources.isEmpty() || allOperations.isEmpty()) {
                log.warn("Recursos ou operações não encontrados. Execute o PermissionService primeiro.");
                return;
            }
            
            // ADMIN: Acesso total a todas as telas
            configureProfilePermissions(adminProfile, allResources, allOperations);
            
            // MANAGER: Apenas tasks, deliveries, billing (quotes - orçamento)
            List<Resource> managerResources = allResources.stream()
                .filter(r -> Arrays.asList("tasks", "deliveries", "billing", "quotes").contains(r.getCode()))
                .toList();
            configureProfilePermissions(managerProfile, managerResources, allOperations);
            
            // USER: Apenas tasks e deliveries
            List<Resource> userResources = allResources.stream()
                .filter(r -> Arrays.asList("tasks", "deliveries").contains(r.getCode()))
                .toList();
            configureProfilePermissions(userProfile, userResources, allOperations);
            
            log.info("Permissões específicas configuradas com sucesso!");
            
        } catch (Exception e) {
            log.error("Erro ao configurar permissões específicas", e);
        }
    }
    
    private void configureProfilePermissions(Profile profile, List<Resource> resources, List<Operation> operations) {
        try {
            for (Resource resource : resources) {
                for (Operation operation : operations) {
                    // Verificar se permissão já existe
                    Optional<ResourcePermission> existing = resourcePermissionRepository
                        .findByProfileIdAndResourceCodeAndOperationCode(
                            profile.getId(), resource.getCode(), operation.getCode());
                    
                    if (existing.isEmpty()) {
                        // Criar permissão
                        ResourcePermission permission = ResourcePermission.builder()
                            .profile(profile)
                            .resource(resource)
                            .operation(operation)
                            .granted(true)
                            .active(true)
                            .build();
                        resourcePermissionRepository.save(permission);
                    }
                }
            }
            log.info("Permissões configuradas para perfil: {}", profile.getCode());
        } catch (Exception e) {
            log.error("Erro ao configurar permissões para perfil: {}", profile.getCode(), e);
        }
    }
}
