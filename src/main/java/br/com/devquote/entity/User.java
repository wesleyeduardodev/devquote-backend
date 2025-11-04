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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserProfile> userProfiles;


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

        if (userProfiles != null) {

            var profileAuthorities = userProfiles.stream()
                    .filter(UserProfile::getActive)
                    .map(up -> new SimpleGrantedAuthority("ROLE_" + up.getProfile().getCode()))
                    .collect(Collectors.toSet());
            authorities.addAll(profileAuthorities);

            var profileSpecificAuthorities = userProfiles.stream()
                    .filter(UserProfile::getActive)
                    .map(up -> new SimpleGrantedAuthority("PROFILE_" + up.getProfile().getCode()))
                    .collect(Collectors.toSet());
            authorities.addAll(profileSpecificAuthorities);
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

    public Set<String> getActiveProfileCodes() {
        if (userProfiles == null) {
            return new HashSet<>();
        }

        return userProfiles.stream()
                .filter(up -> up.getActive())
                .map(up -> up.getProfile().getCode())
                .collect(Collectors.toSet());
    }

    public boolean hasProfile(String profileCode) {
        return getActiveProfileCodes().contains(profileCode);
    }

    public boolean isAdmin() {
        return hasProfile("ADMIN");
    }
}
