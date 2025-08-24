package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private Set<String> roles;
    private Set<String> permissions;
    private Set<String> allowedScreens;

    public JwtResponse(String token, String username, String email, Set<String> roles, Set<String> permissions) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
    }
    
    public JwtResponse(String token, String username, String email, Set<String> roles, Set<String> permissions, Set<String> allowedScreens) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
        this.allowedScreens = allowedScreens;
    }
}