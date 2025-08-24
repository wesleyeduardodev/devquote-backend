package br.com.devquote.service.impl;


import br.com.devquote.configuration.security.JwtUtils;
import br.com.devquote.dto.UserInfoDto;
import br.com.devquote.dto.UserPermissionsDto;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.PermissionService;
import br.com.devquote.service.UserProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserProfileService userProfileService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public MessageResponse registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getFirstName() + " " + signUpRequest.getLastName())
                .active(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);

        // Por padrão, todos os novos usuários recebem o perfil USER
        Profile userProfile = profileRepository.findByCode("USER")
                .orElseThrow(() -> new RuntimeException("Profile USER not found"));

        UserProfileRequest profileRequest = UserProfileRequest.builder()
                .userId(user.getId())
                .profileId(userProfile.getId())
                .active(true)
                .build();

        userProfileService.assignProfileToUser(profileRequest);

        return new MessageResponse("User registered successfully!");
    }

    public UserInfoDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> profiles = currentUser.getActiveProfileCodes();

        Set<String> permissions = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("PROFILE_"))
                .map(authority -> authority.replace("PROFILE_", ""))
                .collect(Collectors.toSet());

        return UserInfoDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .firstName(currentUser.getName())
                .lastName("")
                .roles(profiles)
                .permissions(permissions)
                .build();
    }

    public UserPermissionsDto getUserPermissions(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Usar o PermissionService para obter permissões detalhadas
        var userPermissions = permissionService.getUserPermissions(currentUser.getId());

        Set<String> profiles = currentUser.getActiveProfileCodes();
        Set<String> allowedScreens = userPermissions.getResourcePermissions().keySet();

        return UserPermissionsDto.builder()
                .permissions(profiles) // Códigos dos perfis
                .allowedScreens(allowedScreens) // Recursos (telas) permitidos
                .roles(profiles)
                .resourcePermissions(userPermissions.getResourcePermissions()) // Operações por tela
                .fieldPermissions(userPermissions.getFieldPermissions()) // Permissões de campo
                .build();
    }

    public Set<String> getAllowedScreens(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Usar o PermissionService para obter telas permitidas
        var userPermissions = permissionService.getUserPermissions(currentUser.getId());
        return userPermissions.getResourcePermissions().keySet();
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        log.info("=== TENTATIVA DE AUTENTICAÇÃO ===");
        log.info("Username: {}", loginRequest.getUsername());
        log.info("Password fornecida: {}", loginRequest.getPassword() != null ? "PRESENTE" : "AUSENTE");
        
        // Autenticar o usuário
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.info("AUTENTICAÇÃO REALIZADA COM SUCESSO!");
        } catch (Exception e) {
            log.error("ERRO NA AUTENTICAÇÃO: {}", e.getMessage(), e);
            throw e;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gerar JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obter detalhes do usuário
        User userDetails = (User) authentication.getPrincipal();

        // Obter roles e permissions do novo sistema
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toSet());

        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("PROFILE_"))
                .map(perm -> perm.substring(8)) // Remove "PROFILE_" prefix
                .collect(Collectors.toSet());
        
        // Se não encontrou no novo sistema, tenta o sistema antigo (compatibilidade)
        if (permissions.isEmpty()) {
            permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("SCOPE_"))
                    .map(perm -> perm.substring(6)) // Remove "SCOPE_" prefix
                    .collect(Collectors.toSet());
        }

        // Obter telas permitidas para o usuário
        Set<String> allowedScreens = getAllowedScreens(authentication);
        
        return new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                permissions,
                allowedScreens
        );
    }
}
