package br.com.devquote.controller;

import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.dto.request.ProfileRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.dto.response.ProfileResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.service.PermissionService;
import br.com.devquote.service.UserProfileService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final UserProfileService userProfileService;

    private static final Set<String> ALLOWED_PROFILE_SORT_FIELDS = Set.of(
            "id", "code", "name", "description", "level", "active", "createdAt", "updatedAt"
    );

    // ===== VERIFICAÇÃO DE PERMISSÕES =====

    @GetMapping("/check/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkPermission(
            @PathVariable Long userId,
            @RequestParam String resource,
            @RequestParam String operation) {
        boolean hasPermission = permissionService.hasPermission(userId, resource, operation);
        return ResponseEntity.ok(hasPermission);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPermissionResponse> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    @GetMapping("/field-permission/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getFieldPermission(
            @PathVariable Long userId,
            @RequestParam String resource,
            @RequestParam String field) {
        var permission = permissionService.getFieldPermission(userId, resource, field);
        return ResponseEntity.ok(permission.name());
    }

    // ===== GERENCIAMENTO DE PERFIS =====

    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllProfiles(
            @RequestParam(required = false, defaultValue = "false") boolean paginated,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) MultiValueMap<String, String> params) {
        
        if (!paginated) {
            List<ProfileResponse> profiles = userProfileService.findAllProfiles();
            return ResponseEntity.ok(profiles);
        }
        
        List<String> sortParams = params != null ? params.get("sort") : null;
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_PROFILE_SORT_FIELDS, "id")
        );
        
        Page<ProfileResponse> pageResult = userProfileService.findAllProfilesPaged(
                id, code, name, description, level, active, pageable
        );
        
        PagedResponse<ProfileResponse> response = PageAdapter.toPagedResponseDTO(pageResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.findProfileById(id));
    }

    @PostMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody ProfileRequest request) {
        return new ResponseEntity<>(userProfileService.createProfile(request), HttpStatus.CREATED);
    }

    @PutMapping("/profiles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long id, 
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(id, request));
    }

    @DeleteMapping("/profiles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        userProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/profiles/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfilesBulk(@RequestBody List<Long> ids) {
        userProfileService.deleteProfilesBulk(ids);
        return ResponseEntity.noContent().build();
    }

    // ===== GERENCIAMENTO DE PERFIS DE USUÁRIOS =====

    @GetMapping("/users/{userId}/profiles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProfileResponse>> getUserProfiles(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileService.findUserProfiles(userId));
    }

    @PostMapping("/users/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPermissionResponse> assignProfileToUser(@Valid @RequestBody UserProfileRequest request) {
        return new ResponseEntity<>(userProfileService.assignProfileToUser(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}/profiles/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProfileFromUser(
            @PathVariable Long userId, 
            @PathVariable Long profileId) {
        userProfileService.removeProfileFromUser(userId, profileId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAllProfilesFromUser(@PathVariable Long userId) {
        userProfileService.removeAllProfilesFromUser(userId);
        return ResponseEntity.noContent().build();
    }

    // ===== UTILITÁRIOS =====

    @GetMapping("/users/{userId}/has-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> userHasProfile(
            @PathVariable Long userId,
            @RequestParam String profileCode) {
        boolean hasProfile = userProfileService.userHasProfile(userId, profileCode);
        return ResponseEntity.ok(hasProfile);
    }

    @GetMapping("/is-admin/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.isAdmin(userId));
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> initializeDefaultData() {
        permissionService.initializeDefaultData();
        return ResponseEntity.ok(Map.of("message", "Dados padrão inicializados com sucesso"));
    }
}