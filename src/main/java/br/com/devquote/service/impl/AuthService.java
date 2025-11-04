package br.com.devquote.service.impl;


import br.com.devquote.configuration.security.JwtUtils;
import br.com.devquote.dto.UserInfoDto;
import br.com.devquote.dto.request.LoginRequest;
import br.com.devquote.dto.request.RegisterRequest;
import br.com.devquote.dto.request.UpdateProfileRequest;
import br.com.devquote.dto.request.UserProfileRequest;
import br.com.devquote.dto.response.JwtResponse;
import br.com.devquote.dto.response.MessageResponse;
import br.com.devquote.entity.Profile;
import br.com.devquote.entity.User;
import br.com.devquote.repository.ProfileRepository;
import br.com.devquote.repository.UserRepository;
import br.com.devquote.service.UserProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public MessageResponse registerUser(RegisterRequest signUpRequest) {
        log.info("REGISTER ATTEMPT user={}", signUpRequest.getUsername());
        
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.warn("REGISTER FAILED username already exists: {}", signUpRequest.getUsername());
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("REGISTER FAILED email already exists: {}", signUpRequest.getEmail());
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getFirstName() + " " + signUpRequest.getLastName())
                .active(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);

        Profile userProfile = profileRepository.findByCode("USER")
                .orElseThrow(() -> new RuntimeException("Profile USER not found"));

        UserProfileRequest profileRequest = UserProfileRequest.builder()
                .userId(user.getId())
                .profileId(userProfile.getId())
                .active(true)
                .build();

        userProfileService.assignProfileToUser(profileRequest);
        
        log.info("REGISTER SUCCESS user={} id={}", user.getUsername(), user.getId());
        return new MessageResponse("User registered successfully!");
    }

    public UserInfoDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> profiles = currentUser.getActiveProfileCodes();

        return UserInfoDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .name(currentUser.getName())
                .roles(profiles)
                .build();
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        log.info("LOGIN ATTEMPT user={}", loginRequest.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.info("LOGIN SUCCESS user={}", loginRequest.getUsername());
        } catch (Exception e) {
            log.warn("LOGIN FAILED user={} error={}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(role -> role.substring(5))
                .collect(Collectors.toSet());

        return new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        );
    }
    
    @Transactional
    public MessageResponse updateUserProfile(UpdateProfileRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("PROFILE UPDATE ATTEMPT user={}", username);
        
        User currentUser = userRepository.findByUsernameWithProfiles(username)
                .or(() -> userRepository.findByEmailWithProfiles(username))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            log.warn("PROFILE UPDATE FAILED user={} email already exists: {}", username, request.getEmail());
            throw new RuntimeException("Email is already in use by another user!");
        }

        currentUser.setName(request.getName());
        currentUser.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (request.getPassword().length() < 6) {
                log.warn("PROFILE UPDATE FAILED user={} password too short", username);
                throw new RuntimeException("Password must be at least 6 characters long!");
            }
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                log.warn("PROFILE UPDATE FAILED user={} passwords don't match", username);
                throw new RuntimeException("Passwords do not match!");
            }
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("PASSWORD CHANGED user={}", username);
        }
        
        userRepository.save(currentUser);
        log.info("PROFILE UPDATE SUCCESS user={}", username);
        
        return new MessageResponse("Profile updated successfully! Please login again.");
    }
}
