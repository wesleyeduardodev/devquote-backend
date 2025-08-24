package br.com.devquote.dto.response;

import br.com.devquote.entity.FieldPermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {

    private Long userId;
    private List<ProfileResponse> profiles;
    private Map<String, List<String>> resourcePermissions; // resource -> [operations]
    private Map<String, Map<String, FieldPermissionType>> fieldPermissions; // resource -> field -> permission
}