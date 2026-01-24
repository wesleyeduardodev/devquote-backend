package br.com.devquote.minicurso.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemModuloRequest {

    @NotBlank(message = "Titulo e obrigatorio")
    @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
    private String titulo;

    private String descricao;

    @NotNull(message = "Ordem e obrigatoria")
    private Integer ordem;

    private Integer duracao;

    private Boolean ativo;
}
