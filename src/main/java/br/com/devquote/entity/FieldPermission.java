package br.com.devquote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "resource_id", "field_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class FieldPermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private FieldPermissionType permissionType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}