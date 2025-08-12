package br.com.devquote.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDto {
    private Long id;
    private String name;
    private String description;
    private String screenPath;
}