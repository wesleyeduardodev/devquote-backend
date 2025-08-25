package br.com.devquote.service.impl;

import br.com.devquote.dto.request.ProfileRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.entity.UserProfile;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.PermissionService;
import br.com.devquote.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final ProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public List<ProfileResponse> findAllProfiles() {
        return profileRepository.findAllOrderedByLevel().stream()
            .map(this::toProfileResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ProfileResponse> findAllProfilesPaged(Pageable pageable) {
        Page<Profile> profiles = profileRepository.findAll(pageable);
        return profiles.map(this::toProfileResponse);
    }
    
    @Override
    public Page<ProfileResponse> findAllProfilesPaged(Long id, String code, String name, 
                                                      String description, Integer level, 
                                                      Boolean active, Pageable pageable) {
        Page<Profile> profiles = profileRepository.findAllWithFilters(
            id, code, name, description, level, active, pageable
        );
        return profiles.map(this::toProfileResponse);
    }

    @Override
    public ProfileResponse findProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        return toProfileResponse(profile);
    }

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        if (profileRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Já existe um perfil com este código");
        }

        Profile profile = Profile.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .level(request.getLevel())
            .active(request.getActive())
            .build();

        profile = profileRepository.save(profile);
        return toProfileResponse(profile);
    }

    @Override
    public ProfileResponse updateProfile(Long id, ProfileRequest request) {
        Profile profile = profileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        // Verifica se o código já existe em outro perfil
        Optional<Profile> existingProfile = profileRepository.findByCode(request.getCode());
        if (existingProfile.isPresent() && !existingProfile.get().getId().equals(id)) {
            throw new RuntimeException("Já existe um perfil com este código");
        }

        profile.setCode(request.getCode());
        profile.setName(request.getName());
        profile.setDescription(request.getDescription());
        profile.setLevel(request.getLevel());
        profile.setActive(request.getActive());

        profile = profileRepository.save(profile);
        return toProfileResponse(profile);
    }

    @Override
    public void deleteProfile(Long id) {
        Profile profile = profileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        // Verifica se há usuários usando este perfil
        List<UserProfile> userProfiles = userProfileRepository.findActiveByProfileId(id);
        if (!userProfiles.isEmpty()) {
            throw new RuntimeException("Não é possível excluir perfil que está sendo usado por usuários");
        }

        profileRepository.delete(profile);
    }
    
    @Override
    @Transactional
    public void deleteProfilesBulk(List<Long> ids) {
        for (Long id : ids) {
            try {
                deleteProfile(id);
            } catch (RuntimeException e) {
                // Log o erro mas continua com os outros
                System.err.println("Erro ao deletar perfil " + id + ": " + e.getMessage());
            }
        }
    }

    @Override
    public List<ProfileResponse> findUserProfiles(Long userId) {
        List<UserProfile> userProfiles = userProfileRepository.findActiveByUserId(userId);
        return userProfiles.stream()
            .map(up -> toProfileResponse(up.getProfile()))
            .collect(Collectors.toList());
    }

    @Override
    public UserPermissionResponse assignProfileToUser(UserProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Profile profile = profileRepository.findById(request.getProfileId())
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        // Verifica se já existe a associação
        Optional<UserProfile> existing = userProfileRepository
            .findByUserIdAndProfileId(request.getUserId(), request.getProfileId());

        if (existing.isPresent()) {
            // Se existe mas está inativo, reativa
            UserProfile userProfile = existing.get();
            userProfile.setActive(request.getActive());
            userProfileRepository.save(userProfile);
        } else {
            // Cria nova associação
            UserProfile userProfile = UserProfile.builder()
                .user(user)
                .profile(profile)
                .active(request.getActive())
                .build();
            userProfileRepository.save(userProfile);
        }

        return permissionService.getUserPermissions(request.getUserId());
    }

    @Override
    public void removeProfileFromUser(Long userId, Long profileId) {
        Optional<UserProfile> userProfile = userProfileRepository
            .findByUserIdAndProfileId(userId, profileId);

        if (userProfile.isPresent()) {
            userProfile.get().setActive(false);
            userProfileRepository.save(userProfile.get());
        }
    }

    @Override
    public void removeAllProfilesFromUser(Long userId) {
        List<UserProfile> userProfiles = userProfileRepository.findActiveByUserId(userId);
        userProfiles.forEach(up -> up.setActive(false));
        userProfileRepository.saveAll(userProfiles);
    }

    @Override
    public boolean userHasProfile(Long userId, String profileCode) {
        return userProfileRepository.findActiveByUserId(userId).stream()
            .anyMatch(up -> up.getProfile().getCode().equals(profileCode));
    }

    @Override
    public List<Long> findUserIdsByProfile(String profileCode) {
        Profile profile = profileRepository.findByCode(profileCode)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        return userProfileRepository.findActiveByProfileId(profile.getId()).stream()
            .map(up -> up.getUser().getId())
            .collect(Collectors.toList());
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        return ProfileResponse.builder()
            .id(profile.getId())
            .code(profile.getCode())
            .name(profile.getName())
            .description(profile.getDescription())
            .level(profile.getLevel())
            .active(profile.getActive())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}