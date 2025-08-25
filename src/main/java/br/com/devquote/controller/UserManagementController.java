package br.com.devquote.controller;
import br.com.devquote.dto.*;
import br.com.devquote.service.impl.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userManagementService.findAll(pageable);
        return ResponseEntity.ok(users);
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

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUserPermissions(@PathVariable Long id, @RequestBody UpdatePermissionsDto request) {
        UserDto user = userManagementService.updateUserPermissions(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> permissions = userManagementService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
}
