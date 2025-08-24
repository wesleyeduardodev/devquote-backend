package br.com.devquote.service.impl;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.entity.UserProfile;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
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
        if (request.getRoleNames() != null && !request.getRoleNames().isEmpty()) {
            for (String profileCode : request.getRoleNames()) {
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

        if (request.getRoleNames() != null) {
            // Remove todos os perfis atuais
            userProfileService.removeAllProfilesFromUser(user.getId());
            // Adiciona os novos perfis
            for (String profileCode : request.getRoleNames()) {
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
        for (String profileCode : request.getRoleNames()) {
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
        userRepository.deleteById(id);
    }

    public List<RoleDto> getAllRoles() {
        return profileRepository.findAllOrderedByLevel().stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
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

    private RoleDto convertToRoleDto(Profile profile) {
        return RoleDto.builder()
                .id(profile.getId())
                .name(profile.getCode())
                .description(profile.getDescription())
                .permissions(Set.of(profile.getCode())) // Por enquanto, usando o código do perfil
                .build();
    }
}
