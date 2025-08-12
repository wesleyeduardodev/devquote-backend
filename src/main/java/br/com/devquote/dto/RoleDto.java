package br.com.devquote.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
}