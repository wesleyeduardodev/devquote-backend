package br.com.devquote.configuration.security;
import br.com.devquote.entity.Permission;
import br.com.devquote.entity.Role;
import br.com.devquote.entity.User;
import br.com.devquote.repository.PermissionRepository;
import br.com.devquote.repository.RoleRepository;
import br.com.devquote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class AuthTestUserSeeder {

    private static final Logger log = LoggerFactory.getLogger(AuthTestUserSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedAuthData() {
        return args -> {
            seedAll();
        };
    }

    @Transactional
    protected void seedAll() {
        log.info(">> Seeding permissions, roles and users...");

        // 1) Garantir TODAS as permissions (a partir do enum)
        ensureAllPermissionsFromEnum();

        // 2) Garantir roles com seus conjuntos de permissions
        ensureRoles();

        // 3) Criar/atualizar usuários
        upsertUser(
                "admin@devquote.com", "admin@devquote.com", "admin123",
                "ADMIN", "System", "Administrator"
        );
        upsertUser(
                "dev@devquote.com", "dev@devquote.com", "dev123",
                "DEVELOPER", "John", "Developer"
        );
        upsertUser(
                "user@devquote.com", "user@devquote.com", "user123",
                "USER", "Jane", "User"
        );

        log.info(">> Seeding finalizado.");
    }

    /* -------------------- Permissions -------------------- */

    private void ensureAllPermissionsFromEnum() {
        int created = 0, updated = 0;

        for (Permission.ScreenPermission sp : Permission.ScreenPermission.values()) {
            Optional<Permission> existing = permissionRepository.findByName(sp.getName());
            if (existing.isEmpty()) {
                Permission p = Permission.builder()
                        .name(sp.getName())
                        .description(sp.getDescription())
                        .screenPath(sp.getScreenPath())
                        .build();
                permissionRepository.save(p);
                created++;
            } else {
                Permission p = existing.get();
                boolean changed = false;
                if (!Objects.equals(p.getDescription(), sp.getDescription())) {
                    p.setDescription(sp.getDescription());
                    changed = true;
                }
                if (!Objects.equals(p.getScreenPath(), sp.getScreenPath())) {
                    p.setScreenPath(sp.getScreenPath());
                    changed = true;
                }
                if (changed) {
                    permissionRepository.save(p);
                    updated++;
                }
            }
        }
        log.info("Permissions: created={}, updated={}", created, updated);
    }

    /* -------------------- Roles -------------------- */

    private void ensureRoles() {
        // Conjuntos conforme seus INSERTs
        Set<String> USER_PERMS = setOf(
                "dashboard:view", "projects:view", "tasks:view", "quotes:view"
        );

        Set<String> DEVELOPER_PERMS = setOf(
                "dashboard:view",
                "projects:view", "projects:create", "projects:edit",
                "tasks:view", "tasks:create", "tasks:edit", "tasks:assign",
                "quotes:view", "quotes:create", "quotes:edit",
                "deliveries:view"
        );

        Set<String> MANAGER_PERMS = setOf(
                "dashboard:view",
                "projects:view", "projects:create", "projects:edit", "projects:delete",
                "tasks:view", "tasks:create", "tasks:edit", "tasks:delete", "tasks:assign",
                "quotes:view", "quotes:create", "quotes:edit", "quotes:approve",
                "deliveries:view", "deliveries:manage"
        );

        // ADMIN = todas as permissions
        Set<String> ALL_PERMS = permissionRepository.findAll().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        ensureRoleWithPermissions("USER", "Usuário básico - acesso limitado", USER_PERMS);
        ensureRoleWithPermissions("DEVELOPER", "Desenvolvedor - pode criar e editar", DEVELOPER_PERMS);
        ensureRoleWithPermissions("MANAGER", "Gerente - pode aprovar e gerenciar", MANAGER_PERMS);
        ensureRoleWithPermissions("ADMIN", "Administrador - acesso total", ALL_PERMS);
    }

    private void ensureRoleWithPermissions(String roleName, String description, Set<String> permissionNames) {
        Set<Permission> permEntities = permissionRepository.findByNameIn(permissionNames);
        if (permEntities.size() != permissionNames.size()) {
            // Tenta completar caso existam permissions novas que ainda não estavam persistidas
            Set<String> missing = new HashSet<>(permissionNames);
            permEntities.forEach(p -> missing.remove(p.getName()));
            if (!missing.isEmpty()) {
                // cria as faltantes com desc/path vazios
                for (String m : missing) {
                    Permission p = permissionRepository.findByName(m)
                            .orElseGet(() -> permissionRepository.save(Permission.builder()
                                    .name(m)
                                    .description(null)
                                    .screenPath(null)
                                    .build()));
                    permEntities.add(p);
                }
            }
        }

        Optional<Role> existing = roleRepository.findByNameWithPermissions(roleName);
        if (existing.isEmpty()) {
            Role r = Role.builder()
                    .name(roleName)
                    .description(description)
                    .permissions(permEntities)
                    .build();
            roleRepository.save(r);
            log.info("Role criada: {} ({} perms)", roleName, permEntities.size());
        } else {
            Role r = existing.get();
            r.setDescription(description);
            r.setPermissions(permEntities);
            roleRepository.save(r);
            log.info("Role atualizada: {} ({} perms)", roleName, permEntities.size());
        }
    }

    /* -------------------- Users -------------------- */

    private void upsertUser(String email,
                            String username,
                            String rawPassword,
                            String roleName,
                            String firstName,
                            String lastName) {

        Role role = roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> new IllegalStateException("Role " + roleName + " não encontrada (após ensureRoles)."));

        var opt = userRepository.findByEmail(email.toLowerCase(Locale.ROOT));
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
            log.info("Usuário criado: {} (role: {}) / senha: {}", email, roleName, mask(rawPassword));
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
            log.info("Usuário atualizado: {} (role: {}) / senha: {}", email, roleName, mask(rawPassword));
        }
    }

    /* -------------------- Utils -------------------- */

    private static Set<String> setOf(String... vals) {
        return new LinkedHashSet<>(Arrays.asList(vals));
    }

    private static String mask(String raw) {
        if (raw == null || raw.length() <= 2) return "***";
        return raw.charAt(0) + "***" + raw.charAt(raw.length() - 1);
    }
}
