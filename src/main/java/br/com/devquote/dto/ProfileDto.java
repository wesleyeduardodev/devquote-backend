package br.com.devquote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer level;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Lista de operações permitidas para este perfil
    private List<String> allowedOperations;
    
    // Lista de telas permitidas para este perfil
    private List<String> allowedScreens;
    
    // Número de usuários com este perfil
    private Long userCount;
}