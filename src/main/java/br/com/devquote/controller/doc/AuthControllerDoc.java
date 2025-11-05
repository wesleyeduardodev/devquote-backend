package br.com.devquote.controller.doc;

import br.com.devquote.dto.UserInfoDto;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.request.UpdateProfileRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Authentication")
public interface AuthControllerDoc {

    @Operation(summary = "Authenticate user and return JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT tokens returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    ResponseEntity<?> authenticateUser(
            @Parameter(description = "Login credentials", required = true) @Valid LoginRequest loginRequest);

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or user already exists")
    })
    ResponseEntity<MessageResponse> registerUser(
            @Parameter(description = "Registration data", required = true) @Valid RegisterRequest request);

    @Operation(summary = "Get current authenticated user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    ResponseEntity<UserInfoDto> getCurrentUser(
            @Parameter(description = "Authenticated user", hidden = true) Authentication authentication);

    @Operation(summary = "Logout current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    ResponseEntity<MessageResponse> logout();

    @Operation(summary = "Update user profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid profile data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    ResponseEntity<MessageResponse> updateProfile(
            @Parameter(description = "Profile update data", required = true) @Valid UpdateProfileRequest request,
            @Parameter(description = "Authenticated user", hidden = true) Authentication authentication);
}
