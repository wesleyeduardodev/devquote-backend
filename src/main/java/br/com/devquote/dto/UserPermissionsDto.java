package br.com.devquote.dto;
import br.com.devquote.entity.FieldPermissionType;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class UserPermissionsDto {
    private Set<String> permissions;
    private Set<String> allowedScreens;
    private Set<String> roles;
    
    // Permissões detalhadas por recurso (tela)
    private Map<String, List<String>> resourcePermissions;
    
    // Permissões de campo (se houver)
    private Map<String, Map<String, FieldPermissionType>> fieldPermissions;
}