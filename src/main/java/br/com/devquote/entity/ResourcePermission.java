package br.com.devquote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "resource_id", "operation_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ResourcePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean granted = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}