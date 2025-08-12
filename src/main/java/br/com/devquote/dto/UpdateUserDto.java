package br.com.devquote.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Set<String> roleNames;
}
