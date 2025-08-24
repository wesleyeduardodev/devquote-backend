package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer level;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}