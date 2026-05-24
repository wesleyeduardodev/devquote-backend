package br.com.devquote.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload final do wizard de setup do ClickUp.
 * O backend persiste cada campo no system_parameter correspondente.
 */
@Data
public class ClickUpSetupSaveRequest {
    @NotBlank(message = "Token é obrigatório")
    private String token;

    @NotBlank(message = "ID da lista é obrigatório")
    private String listId;

    @NotBlank(message = "ID do campo Desenvolvedor é obrigatório")
    private String developerFieldId;

    @NotBlank(message = "ID da opção (você) no campo Desenvolvedor é obrigatório")
    private String developerOptionId;

    /** Opcional — pode ser null/vazio se a lista não tiver campo de ordenação. */
    private String orderFieldId;
}
