package br.com.devquote.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @Column(name = "name")
    private String name;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Builder.Default
    @Column(name = "account_non_expired")
    private Boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "account_non_locked")
    private Boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "credentials_non_expired")
    private Boolean credentialsNonExpired = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento com o novo sistema de perfis
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserProfile> userProfiles;

    // Mantendo compatibilidade com sistema anterior (opcional - pode ser removido futuramente)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // NOVO SISTEMA: Authorities baseadas em perfis
        if (userProfiles != null) {
            // ROLE_ para os perfis (ROLE_ADMIN, ROLE_MANAGER, ROLE_USER)
            var profileAuthorities = userProfiles.stream()
                    .filter(up -> up.getActive())
                    .map(up -> new SimpleGrantedAuthority("ROLE_" + up.getProfile().getCode()))
                    .collect(Collectors.toSet());
            authorities.addAll(profileAuthorities);
            
            // PROFILE_ para identificação específica de perfil
            var profileSpecificAuthorities = userProfiles.stream()
                    .filter(up -> up.getActive())
                    .map(up -> new SimpleGrantedAuthority("PROFILE_" + up.getProfile().getCode()))
                    .collect(Collectors.toSet());
            authorities.addAll(profileSpecificAuthorities);
        }
        
        // SISTEMA ANTERIOR: Mantendo compatibilidade (pode ser removido futuramente)
        if (roles != null) {
            // ROLE_ para os papéis
            var roleAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toSet());
            authorities.addAll(roleAuthorities);

            // SCOPE_ para as permissões (como você já usa nos matchers)
            var scopeAuthorities = roles.stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> new SimpleGrantedAuthority("SCOPE_" + permission.getName()))
                    .collect(Collectors.toSet());
            authorities.addAll(scopeAuthorities);
        }
        
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return active != null ? active : true;
    }
    
    // Método auxiliar para obter perfis ativos
    public Set<String> getActiveProfileCodes() {
        if (userProfiles == null) {
            return new HashSet<>();
        }
        
        return userProfiles.stream()
                .filter(up -> up.getActive())
                .map(up -> up.getProfile().getCode())
                .collect(Collectors.toSet());
    }
    
    // Método auxiliar para verificar se tem um perfil específico
    public boolean hasProfile(String profileCode) {
        return getActiveProfileCodes().contains(profileCode);
    }
    
    // Método auxiliar para verificar se é admin
    public boolean isAdmin() {
        return hasProfile("ADMIN");
    }
}