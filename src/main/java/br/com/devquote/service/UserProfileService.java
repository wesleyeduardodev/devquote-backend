package br.com.devquote.service;

import br.com.devquote.dto.request.ProfileRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;

import java.util.List;

public interface UserProfileService {

    // Gerenciamento de perfis
    List<ProfileResponse> findAllProfiles();
    ProfileResponse findProfileById(Long id);
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse updateProfile(Long id, ProfileRequest request);
    void deleteProfile(Long id);

    // Gerenciamento de perfis de usuários
    List<ProfileResponse> findUserProfiles(Long userId);
    UserPermissionResponse assignProfileToUser(UserProfileRequest request);
    void removeProfileFromUser(Long userId, Long profileId);
    void removeAllProfilesFromUser(Long userId);

    // Utilitários
    boolean userHasProfile(Long userId, String profileCode);
    List<Long> findUserIdsByProfile(String profileCode);
}