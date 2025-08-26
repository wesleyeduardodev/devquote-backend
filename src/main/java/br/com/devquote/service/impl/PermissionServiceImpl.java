package br.com.devquote.service.impl;
import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.entity.*;
import br.com.devquote.enums.ProfileType;
import br.com.devquote.repository.*;
import br.com.devquote.service.PermissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserProfileRepository userProfileRepository;
    private final ResourcePermissionRepository resourcePermissionRepository;
    private final FieldPermissionRepository fieldPermissionRepository;

    @Override
    public boolean hasPermission(Long userId, String resourceCode, String operationCode) {
        return resourcePermissionRepository.hasUserPermission(userId, resourceCode, operationCode);
    }

    @Override
    public boolean canEditField(Long userId, String resourceCode, String fieldName) {
        FieldPermissionType permission = getFieldPermission(userId, resourceCode, fieldName);
        return permission == FieldPermissionType.EDIT;
    }

    @Override
    public FieldPermissionType getFieldPermission(Long userId, String resourceCode, String fieldName) {
        List<FieldPermissionType> permissions = fieldPermissionRepository
                .findUserFieldPermissionTypes(userId, resourceCode, fieldName);

        if (permissions.isEmpty()) {
            // Se não há permissão específica definida, assume EDIT por padrão
            return FieldPermissionType.EDIT;
        }

        // Retorna a permissão do perfil com maior privilégio (menor level)
        // HIDDEN > READ > EDIT (ordem de prioridade)
        if (permissions.contains(FieldPermissionType.HIDDEN)) {
            return FieldPermissionType.HIDDEN;
        }
        if (permissions.contains(FieldPermissionType.READ)) {
            return FieldPermissionType.READ;
        }
        return FieldPermissionType.EDIT;
    }

    @Override
    public UserPermissionResponse getUserPermissions(Long userId) {
        List<UserProfile> userProfiles = userProfileRepository.findActiveByUserId(userId);

        List<ProfileResponse> profiles = userProfiles.stream()
                .map(up -> ProfileResponse.builder()
                        .id(up.getProfile().getId())
                        .code(up.getProfile().getCode())
                        .name(up.getProfile().getName())
                        .description(up.getProfile().getDescription())
                        .level(up.getProfile().getLevel())
                        .active(up.getProfile().getActive())
                        .createdAt(up.getProfile().getCreatedAt())
                        .updatedAt(up.getProfile().getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        // Coletar permissões de recursos
        Map<String, Set<String>> resourcePermissionsMap = new HashMap<>();
        for (UserProfile userProfile : userProfiles) {
            List<ResourcePermission> permissions = resourcePermissionRepository
                    .findActiveByProfileId(userProfile.getProfile().getId());

            for (ResourcePermission permission : permissions) {
                if (permission.getGranted()) {
                    resourcePermissionsMap
                            .computeIfAbsent(permission.getResource().getCode(), k -> new HashSet<>())
                            .add(permission.getOperation().getCode());
                }
            }
        }

        Map<String, List<String>> resourcePermissions = resourcePermissionsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                ));

        // Coletar permissões de campos
        Map<String, Map<String, FieldPermissionType>> fieldPermissions = new HashMap<>();
        for (UserProfile userProfile : userProfiles) {
            List<FieldPermission> permissions = fieldPermissionRepository
                    .findActiveByProfileId(userProfile.getProfile().getId());

            for (FieldPermission permission : permissions) {
                fieldPermissions
                        .computeIfAbsent(permission.getResource().getCode(), k -> new HashMap<>())
                        .put(permission.getFieldName(), permission.getPermissionType());
            }
        }

        return UserPermissionResponse.builder()
                .userId(userId)
                .profiles(profiles)
                .resourcePermissions(resourcePermissions)
                .fieldPermissions(fieldPermissions)
                .build();
    }

    @Override
    public boolean hasAnyProfile(Long userId, List<String> profileCodes) {
        List<UserProfile> userProfiles = userProfileRepository.findActiveByUserId(userId);
        Set<String> userProfileCodes = userProfiles.stream()
                .map(up -> up.getProfile().getCode())
                .collect(Collectors.toSet());

        return profileCodes.stream().anyMatch(userProfileCodes::contains);
    }

    @Override
    public boolean isAdmin(Long userId) {
        return hasAnyProfile(userId, List.of(ProfileType.ADMIN.getCode()));
    }
}
