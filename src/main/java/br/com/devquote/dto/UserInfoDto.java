package br.com.devquote.dto;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserInfoDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Set<String> roles;
    private Set<String> permissions;
}