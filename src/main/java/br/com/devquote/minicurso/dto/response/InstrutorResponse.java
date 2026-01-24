package br.com.devquote.minicurso.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrutorResponse {

    private Long id;
    private String nome;
    private String localTrabalho;
    private String tempoCarreira;
    private String miniBio;
    private String fotoUrl;
    private String email;
    private String linkedin;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ModuloSimplificadoResponse> modulos;
}
