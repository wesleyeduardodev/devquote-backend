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

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    public boolean isCurrentUserAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }

    /**
     * Define quem pode visualizar valores monetários (amount/taskValue) na API e relatórios.
     * Regra: ADMIN e MANAGER podem; USER não pode.
     * Motivo: o fluxo de Faturamento é acessível a MANAGER, que precisa ver valores para faturar.
     */
    public boolean canViewMonetaryValues() {
        return isCurrentUserAdmin() || hasAuthority("ROLE_MANAGER");
    }

    /** true quando o usuário atual NÃO pode ver valores monetários (perfil USER). */
    public boolean cannotViewMonetaryValues() {
        return !canViewMonetaryValues();
    }
}