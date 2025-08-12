package br.com.devquote.controller;
import br.com.devquote.dto.*;
import br.com.devquote.dto.request.LoginRequestDto;
import br.com.devquote.dto.request.RegisterRequestDto;
import br.com.devquote.dto.response.JwtResponseDto;
import br.com.devquote.dto.response.MessageResponseDto;
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
// @CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            JwtResponseDto jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDto("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto request) {
        try {
            MessageResponseDto response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        UserInfoDto userInfo = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/permissions")
    public ResponseEntity<UserPermissionsDto> getUserPermissions(Authentication authentication) {
        UserPermissionsDto permissions = authService.getUserPermissions(authentication);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/screens")
    public ResponseEntity<Set<String>> getAllowedScreens(Authentication authentication) {
        Set<String> allowedScreens = authService.getAllowedScreens(authentication);
        return ResponseEntity.ok(allowedScreens);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout() {
        return ResponseEntity.ok(new MessageResponseDto("Logged out successfully!"));
    }
}