package br.com.devquote.service.impl;

import br.com.devquote.configuration.security.JwtUtils;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import br.com.devquote.entity.Permission;
import br.com.devquote.entity.Role;
import br.com.devquote.entity.User;
import br.com.devquote.repository.RoleRepository;
import br.com.devquote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Por padrão, todos os novos usuários recebem a role USER
        Role userRole = roleRepository.findByNameWithPermissions("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));

        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }

    public UserInfoDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithRolesAndPermissions(username)
                .or(() -> userRepository.findByEmailWithRolesAndPermissions(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> roles = currentUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("SCOPE_", ""))
                .collect(Collectors.toSet());

        return UserInfoDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public UserPermissionsDto getUserPermissions(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithRolesAndPermissions(username)
                .or(() -> userRepository.findByEmailWithRolesAndPermissions(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> permissions = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("SCOPE_", ""))
                .collect(Collectors.toSet());

        Set<String> allowedScreens = currentUser.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getScreenPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> roles = currentUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserPermissionsDto.builder()
                .permissions(permissions)
                .allowedScreens(allowedScreens)
                .roles(roles)
                .build();
    }

    public Set<String> getAllowedScreens(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithRolesAndPermissions(username)
                .or(() -> userRepository.findByEmailWithRolesAndPermissions(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return currentUser.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getScreenPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Autenticar o usuário
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gerar JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obter detalhes do usuário
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Obter roles e permissions
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toSet());

        Set<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("SCOPE_"))
                .map(perm -> perm.substring(6)) // Remove "SCOPE_" prefix
                .collect(Collectors.toSet());

        return new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                permissions
        );
    }
}