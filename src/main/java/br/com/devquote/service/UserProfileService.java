package br.com.devquote.service;
import br.com.devquote.dto.request.UserProfileRequest;

public interface UserProfileService {
    void deleteProfile(Long id);
    void assignProfileToUser(UserProfileRequest request);
    void removeAllProfilesFromUser(Long userId);
}