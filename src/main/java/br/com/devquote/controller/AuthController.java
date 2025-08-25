package br.com.devquote.controller;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            MessageResponse response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        UserInfoDto userInfo = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/permissions")
    public ResponseEntity<UserPermissionResponse> getUserPermissions(Authentication authentication) {
        UserPermissionResponse permissions = authService.getUserPermissions(authentication);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/screens")
    public ResponseEntity<Set<String>> getAllowedScreens(Authentication authentication) {
        Set<String> allowedScreens = authService.getAllowedScreens(authentication);
        return ResponseEntity.ok(allowedScreens);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(new MessageResponse("Logged out successfully!"));
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debug = new HashMap<>();
        
        if (authentication != null) {
            debug.put("username", authentication.getName());
            debug.put("isAuthenticated", authentication.isAuthenticated());
            debug.put("principalClass", authentication.getPrincipal().getClass().getSimpleName());
            
            // Get the User principal
            if (authentication.getPrincipal() instanceof br.com.devquote.entity.User) {
                br.com.devquote.entity.User user = (br.com.devquote.entity.User) authentication.getPrincipal();
                debug.put("userId", user.getId());
                debug.put("userActive", user.getActive());
                debug.put("userProfiles", user.getUserProfiles() != null ? user.getUserProfiles().size() : 0);
                
                // Debug user profiles
                if (user.getUserProfiles() != null) {
                    debug.put("profileDetails", user.getUserProfiles().stream()
                            .map(up -> {
                                Map<String, Object> profileMap = new HashMap<>();
                                profileMap.put("profileId", up.getProfile() != null ? up.getProfile().getId() : null);
                                profileMap.put("profileCode", up.getProfile() != null ? up.getProfile().getCode() : null);
                                profileMap.put("profileName", up.getProfile() != null ? up.getProfile().getName() : null);
                                profileMap.put("isActive", up.getActive());
                                return profileMap;
                            })
                            .collect(Collectors.toList()));
                }
                
                // Get active profile codes
                debug.put("activeProfileCodes", user.getActiveProfileCodes());
            }
            
            debug.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            
            // Check specific authorities
            boolean hasRoleAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean hasAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));
            boolean hasAuthorityRoleAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            debug.put("hasRoleAdmin", hasRoleAdmin);
            debug.put("hasAdmin", hasAdmin);
            debug.put("hasAuthorityRoleAdmin", hasAuthorityRoleAdmin);
            
            log.info("Debug Auth - User: {}, Authorities: {}, Principal Class: {}", 
                    authentication.getName(), 
                    authentication.getAuthorities(),
                    authentication.getPrincipal().getClass().getName());
        } else {
            debug.put("error", "No authentication found");
        }
        
        return ResponseEntity.ok(debug);
    }
}