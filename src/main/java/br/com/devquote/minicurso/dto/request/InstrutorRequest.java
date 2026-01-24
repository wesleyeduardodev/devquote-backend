package br.com.devquote.minicurso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrutorRequest {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 150, message = "Nome deve ter no maximo 150 caracteres")
    private String nome;

    @Size(max = 200, message = "Local de trabalho deve ter no maximo 200 caracteres")
    private String localTrabalho;

    @Size(max = 50, message = "Tempo de carreira deve ter no maximo 50 caracteres")
    private String tempoCarreira;

    @Size(max = 1000, message = "Mini bio deve ter no maximo 1000 caracteres")
    private String miniBio;

    @Email(message = "Email invalido")
    @Size(max = 150, message = "Email deve ter no maximo 150 caracteres")
    private String email;

    @Size(max = 200, message = "LinkedIn deve ter no maximo 200 caracteres")
    private String linkedin;

    private Boolean ativo;

    private List<Long> modulosIds;
}
