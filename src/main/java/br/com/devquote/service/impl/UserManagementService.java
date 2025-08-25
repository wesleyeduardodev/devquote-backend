package br.com.devquote.service.impl;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.entity.Permission;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.entity.UserProfile;
import br.com.devquote.repository.PermissionRepository;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PermissionRepository permissionRepository;
    private final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    public Page<UserDto> findAllWithFilters(Long id, String username, String email, 
                                           String firstName, String lastName, Boolean enabled, 
                                           Pageable pageable) {
        // Como o User tem apenas o campo "name" e não firstName/lastName separados,
        // vamos combinar firstName e lastName para buscar no campo name
        String name = null;
        if (firstName != null && !firstName.isEmpty()) {
            name = firstName;
            if (lastName != null && !lastName.isEmpty()) {
                name = name + " " + lastName;
            }
        } else if (lastName != null && !lastName.isEmpty()) {
            name = lastName;
        }
        
        return userRepository.findAllWithFilters(id, username, email, name, enabled, pageable)
                .map(this::convertToDto);
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getFirstName() + " " + request.getLastName())
                .active(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);
        
        // Atribuir perfis ao usuário
        if (request.getProfileCodes() != null && !request.getProfileCodes().isEmpty()) {
            for (String profileCode : request.getProfileCodes()) {
                Profile profile = profileRepository.findByCode(profileCode)
                        .orElseThrow(() -> new RuntimeException("Profile not found: " + profileCode));
                
                UserProfileRequest profileRequest = UserProfileRequest.builder()
                        .userId(user.getId())
                        .profileId(profile.getId())
                        .active(true)
                        .build();
                
                userProfileService.assignProfileToUser(profileRequest);
            }
        }

        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getFirstName() + " " + request.getLastName());
        user.setActive(request.getEnabled());

        if (request.getProfileCodes() != null) {
            // Remove todos os perfis atuais
            userProfileService.removeAllProfilesFromUser(user.getId());
            // Adiciona os novos perfis
            for (String profileCode : request.getProfileCodes()) {
                Profile profile = profileRepository.findByCode(profileCode)
                        .orElseThrow(() -> new RuntimeException("Profile not found: " + profileCode));
                
                UserProfileRequest profileRequest = UserProfileRequest.builder()
                        .userId(user.getId())
                        .profileId(profile.getId())
                        .active(true)
                        .build();
                
                userProfileService.assignProfileToUser(profileRequest);
            }
        }

        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Transactional
    public UserDto updateUserPermissions(Long id, UpdatePermissionsDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove todos os perfis atuais
        userProfileService.removeAllProfilesFromUser(user.getId());
        // Adiciona os novos perfis
        for (String profileCode : request.getProfileCodes()) {
            Profile profile = profileRepository.findByCode(profileCode)
                    .orElseThrow(() -> new RuntimeException("Profile not found: " + profileCode));
            
            UserProfileRequest profileRequest = UserProfileRequest.builder()
                    .userId(user.getId())
                    .profileId(profile.getId())
                    .active(true)
                    .build();
            
            userProfileService.assignProfileToUser(profileRequest);
        }

        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        
        // Verifica se está tentando deletar o próprio usuário
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
            if (currentUser != null && currentUser.getId().equals(id)) {
                throw new RuntimeException("Você não pode excluir sua própria conta");
            }
        }
        
        // Remove todos os perfis do usuário antes de deletar
        userProfileService.removeAllProfilesFromUser(id);
        
        // Deleta o usuário
        userRepository.deleteById(id);
    }
    
    @Transactional
    public void deleteBulk(List<Long> ids) {
        // Verifica se o usuário atual está na lista
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User currentUser = userRepository.findByUsername(authentication.getName())
                    .orElse(null);
            if (currentUser != null && ids.contains(currentUser.getId())) {
                throw new RuntimeException("Você não pode excluir sua própria conta");
            }
        }
        
        for (Long id : ids) {
            if (userRepository.existsById(id)) {
                // Remove todos os perfis e permissões do usuário antes de deletar
                userProfileService.removeAllProfilesFromUser(id);
                
                // Deleta o usuário
                userRepository.deleteById(id);
            }
        }
    }

    public List<PermissionDto> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAllOrderedById();
        return permissions.stream()
                .map(this::convertPermissionToDto)
                .collect(Collectors.toList());
    }

    private PermissionDto convertPermissionToDto(Permission permission) {
        return PermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .screenPath(permission.getScreenPath())
                .build();
    }

    private UserDto convertToDto(User user) {
        Set<String> profileCodes = user.getActiveProfileCodes();
        
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getName())
                .lastName("")
                .enabled(user.getActive())
                .roles(profileCodes)
                .permissions(profileCodes) // Por enquanto, usando os códigos dos perfis como permissions
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
