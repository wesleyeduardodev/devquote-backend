package br.com.devquote.minicurso.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InscricaoRequest {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
    private String nome;

    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email deve estar em formato valido")
    @Size(max = 150, message = "Email deve ter no maximo 150 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres")
    @Pattern(regexp = "^$|^\\+?[0-9\\-().\\s]*$", message = "Telefone invalido")
    private String telefone;

    @NotBlank(message = "Curso e obrigatorio")
    @Size(max = 100, message = "Curso deve ter no maximo 100 caracteres")
    private String curso;

    @Size(max = 20, message = "Periodo deve ter no maximo 20 caracteres")
    private String periodo;

    @NotBlank(message = "Nivel de programacao e obrigatorio")
    @Pattern(regexp = "^(INICIANTE|INTERMEDIARIO|AVANCADO)$", message = "Nivel deve ser: INICIANTE, INTERMEDIARIO ou AVANCADO")
    private String nivelProgramacao;

    @Size(max = 500, message = "Expectativa deve ter no maximo 500 caracteres")
    private String expectativa;
}
