package br.com.devquote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "profile_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}