package br.com.devquote.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdatePermissionsDto {
    private Set<String> profileCodes;
}