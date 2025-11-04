package br.com.devquote.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Boolean enabled;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}