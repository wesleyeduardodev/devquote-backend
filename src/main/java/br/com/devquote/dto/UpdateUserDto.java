package br.com.devquote.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserDto {
    private String username;
    private String email;
    private String name;
    private String firstName; // Deprecated - manter para compatibilidade
    private String lastName; // Deprecated - manter para compatibilidade
    private Boolean enabled;
    private Set<String> profileCodes;
}
