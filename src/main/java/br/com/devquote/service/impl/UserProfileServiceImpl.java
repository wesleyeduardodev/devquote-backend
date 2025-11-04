package br.com.devquote.service.impl;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.entity.UserProfile;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final ProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public void deleteProfile(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        List<UserProfile> userProfiles = userProfileRepository.findActiveByProfileId(id);
        if (!userProfiles.isEmpty()) {
            throw new RuntimeException("Não é possível excluir perfil que está sendo usado por usuários");
        }
        profileRepository.delete(profile);
    }

    @Override
    public void assignProfileToUser(UserProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        Optional<UserProfile> existing = userProfileRepository
                .findByUserIdAndProfileId(request.getUserId(), request.getProfileId());

        if (existing.isPresent()) {

            UserProfile userProfile = existing.get();
            userProfile.setActive(request.getActive());
            userProfileRepository.save(userProfile);
        } else {

            UserProfile userProfile = UserProfile.builder()
                    .user(user)
                    .profile(profile)
                    .active(request.getActive())
                    .build();
            userProfileRepository.save(userProfile);
        }
    }

    @Override
    public void removeAllProfilesFromUser(Long userId) {
        List<UserProfile> userProfiles = userProfileRepository.findByUserId(userId);
        userProfileRepository.deleteAll(userProfiles);
    }
}
