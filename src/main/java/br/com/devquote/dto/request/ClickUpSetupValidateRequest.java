package br.com.devquote.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClickUpSetupValidateRequest {
    @NotBlank(message = "Token é obrigatório")
    private String token;
}
