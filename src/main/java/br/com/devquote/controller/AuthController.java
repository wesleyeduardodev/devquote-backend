package br.com.devquote.controller;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.request.UpdateProfileRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import br.com.devquote.dto.response.UserPermissionResponse;
import br.com.devquote.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
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
    
    @PutMapping("/profile")
    public ResponseEntity<MessageResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        try {
            MessageResponse response = authService.updateUserProfile(request, authentication);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}