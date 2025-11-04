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

    public JwtResponse(String token, String username, String email, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}