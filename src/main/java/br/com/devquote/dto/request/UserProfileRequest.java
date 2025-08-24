package br.com.devquote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private Long userId;

    @NotNull(message = "ID do perfil é obrigatório")
    private Long profileId;

    @Builder.Default
    private Boolean active = true;
}