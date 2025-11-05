package br.com.devquote.controller;

import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.controller.doc.UserManagementControllerDoc;
import br.com.devquote.dto.*;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.service.impl.UserManagementService;
import br.com.devquote.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController implements UserManagementControllerDoc {

    private final UserManagementService userManagementService;
    
    private static final Set<String> ALLOWED_USER_SORT_FIELDS = Set.of(
            "id", "username", "email", "firstName", "lastName", "enabled", "createdAt", "updatedAt"
    );

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PagedResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) MultiValueMap<String, String> params) {
        
        List<String> sortParams = params != null ? params.get("sort") : null;
        
        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_USER_SORT_FIELDS, "id")
        );
        
        Page<UserDto> pageResult = userManagementService.findAllWithFilters(
                id, username, email, name, enabled, pageable
        );
        
        PagedResponse<UserDto> response = PageAdapter.toPagedResponseDTO(pageResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userManagementService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto request) {
        UserDto user = userManagementService.createUser(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto request) {
        UserDto user = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        userManagementService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id) {
        userManagementService.resetPassword(id);
        return ResponseEntity.ok().build();
    }
}
