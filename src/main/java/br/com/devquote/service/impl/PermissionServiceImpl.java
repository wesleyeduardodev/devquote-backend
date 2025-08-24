package br.com.devquote.service.impl;

import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.entity.*;
import br.com.devquote.enums.OperationType;
import br.com.devquote.enums.ProfileType;
import br.com.devquote.enums.ResourceType;
import br.com.devquote.repository.*;
import br.com.devquote.service.PermissionService;
import jakarta.annotation.PostConstruct;
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
    private final ProfileRepository profileRepository;
    private final ResourceRepository resourceRepository;
    private final OperationRepository operationRepository;

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

    @PostConstruct
    @Override
    public void initializeDefaultData() {
        try {
            log.info("Inicializando dados padrão do sistema de permissões...");
            
            // Criar perfis padrão
            createDefaultProfiles();
            
            // Criar recursos padrão
            createDefaultResources();
            
            // Criar operações padrão
            createDefaultOperations();
            
            // Criar permissões padrão
            createDefaultPermissions();
            
            log.info("Dados padrão do sistema de permissões inicializados com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao inicializar dados padrão do sistema de permissões", e);
        }
    }

    private void createDefaultProfiles() {
        for (ProfileType profileType : ProfileType.values()) {
            if (!profileRepository.existsByCode(profileType.getCode())) {
                Profile profile = Profile.builder()
                    .code(profileType.getCode())
                    .name(profileType.getName())
                    .description(profileType.getDescription())
                    .level(profileType.getLevel())
                    .active(true)
                    .build();
                profileRepository.save(profile);
                log.info("Perfil criado: {}", profile.getCode());
            }
        }
    }

    private void createDefaultResources() {
        for (ResourceType resourceType : ResourceType.values()) {
            if (!resourceRepository.existsByCode(resourceType.getCode())) {
                Resource resource = Resource.builder()
                    .code(resourceType.getCode())
                    .name(resourceType.getName())
                    .description(resourceType.getDescription())
                    .active(true)
                    .build();
                resourceRepository.save(resource);
                log.info("Recurso criado: {}", resource.getCode());
            }
        }
    }

    private void createDefaultOperations() {
        for (OperationType operationType : OperationType.values()) {
            if (!operationRepository.existsByCode(operationType.getCode())) {
                Operation operation = Operation.builder()
                    .code(operationType.getCode())
                    .name(operationType.getName())
                    .description(operationType.getDescription())
                    .active(true)
                    .build();
                operationRepository.save(operation);
                log.info("Operação criada: {}", operation.getCode());
            }
        }
    }

    private void createDefaultPermissions() {
        // Buscar entidades necessárias
        Profile adminProfile = profileRepository.findByCode(ProfileType.ADMIN.getCode()).orElse(null);
        Profile managerProfile = profileRepository.findByCode(ProfileType.MANAGER.getCode()).orElse(null);
        Profile userProfile = profileRepository.findByCode(ProfileType.USER.getCode()).orElse(null);

        if (adminProfile == null || managerProfile == null || userProfile == null) {
            log.warn("Perfis não encontrados para criação de permissões padrão");
            return;
        }

        List<Resource> resources = resourceRepository.findAllOrderedByName();
        List<Operation> operations = operationRepository.findAllOrderedByName();

        // ADMIN: Todas as permissões
        createProfilePermissions(adminProfile, resources, operations);

        // MANAGER: Permissões de gestão (exceto usuários e configurações)
        List<Resource> managerResources = resources.stream()
            .filter(r -> !r.getCode().equals(ResourceType.USERS.getCode()) && 
                        !r.getCode().equals(ResourceType.SETTINGS.getCode()))
            .collect(Collectors.toList());
        createProfilePermissions(managerProfile, managerResources, operations);

        // USER: Apenas leitura e operações básicas
        List<Operation> userOperations = operations.stream()
            .filter(o -> o.getCode().equals(OperationType.READ.getCode()) || 
                        o.getCode().equals(OperationType.CREATE.getCode()))
            .collect(Collectors.toList());
        
        List<Resource> userResources = resources.stream()
            .filter(r -> !r.getCode().equals(ResourceType.USERS.getCode()) && 
                        !r.getCode().equals(ResourceType.SETTINGS.getCode()) &&
                        !r.getCode().equals(ResourceType.BILLING.getCode()))
            .collect(Collectors.toList());
        createProfilePermissions(userProfile, userResources, userOperations);
    }

    private void createProfilePermissions(Profile profile, List<Resource> resources, List<Operation> operations) {
        for (Resource resource : resources) {
            for (Operation operation : operations) {
                // Verifica se já existe
                Optional<ResourcePermission> existing = resourcePermissionRepository
                    .findByProfileIdAndResourceCodeAndOperationCode(
                        profile.getId(), resource.getCode(), operation.getCode());
                
                if (existing.isEmpty()) {
                    ResourcePermission permission = ResourcePermission.builder()
                        .profile(profile)
                        .resource(resource)
                        .operation(operation)
                        .granted(true)
                        .active(true)
                        .build();
                    resourcePermissionRepository.save(permission);
                }
            }
        }
        log.info("Permissões criadas para perfil: {}", profile.getCode());
    }
}