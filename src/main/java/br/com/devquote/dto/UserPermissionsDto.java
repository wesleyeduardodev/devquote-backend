package br.com.devquote.dto;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserPermissionsDto {
    private Set<String> permissions;
    private Set<String> allowedScreens;
    private Set<String> roles;
}