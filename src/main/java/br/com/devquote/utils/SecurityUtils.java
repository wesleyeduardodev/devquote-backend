package br.com.devquote.utils;

import br.com.devquote.entity.User;
import br.com.devquote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public boolean isCurrentUserAdmin() {
        User user = getCurrentUser();
        return user != null && user.getActiveProfileCodes().contains("ADMIN");
    }

    public boolean isCurrentUserManagerOrAdmin() {
        User user = getCurrentUser();
        if (user == null) return false;
        
        var profiles = user.getActiveProfileCodes();
        return profiles.contains("ADMIN") || profiles.contains("MANAGER");
    }

    public boolean isCurrentUserOwnerOrAdmin(Long createdByUserId) {
        User user = getCurrentUser();
        if (user == null) return false;
        
        if (user.getActiveProfileCodes().contains("ADMIN")) {
            return true;
        }
        
        return user.getId().equals(createdByUserId);
    }
}