package br.com.devquote.service;
import br.com.devquote.dto.request.UserProfileRequest;

public interface UserProfileService {
    void assignProfileToUser(UserProfileRequest request);
    void removeAllProfilesFromUser(Long userId);
}