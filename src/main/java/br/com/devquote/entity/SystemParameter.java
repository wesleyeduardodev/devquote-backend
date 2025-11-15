package br.com.devquote.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_parameter", uniqueConstraints = {
    @UniqueConstraint(name = "uk_system_parameter_name", columnNames = {"name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemParameter extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "description", length = 255)
    private String description;
}
