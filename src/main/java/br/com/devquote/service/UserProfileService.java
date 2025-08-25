package br.com.devquote.service;

import br.com.devquote.dto.request.ProfileRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserProfileService {

    // Gerenciamento de perfis
    List<ProfileResponse> findAllProfiles();
    Page<ProfileResponse> findAllProfilesPaged(Pageable pageable);
    Page<ProfileResponse> findAllProfilesPaged(Long id, String code, String name, String description, Integer level, Boolean active, Pageable pageable);
    ProfileResponse findProfileById(Long id);
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse updateProfile(Long id, ProfileRequest request);
    void deleteProfile(Long id);
    void deleteProfilesBulk(List<Long> ids);

    // Gerenciamento de perfis de usuários
    List<ProfileResponse> findUserProfiles(Long userId);
    UserPermissionResponse assignProfileToUser(UserProfileRequest request);
    void removeProfileFromUser(Long userId, Long profileId);
    void removeAllProfilesFromUser(Long userId);

    // Utilitários
    boolean userHasProfile(Long userId, String profileCode);
    List<Long> findUserIdsByProfile(String profileCode);
}