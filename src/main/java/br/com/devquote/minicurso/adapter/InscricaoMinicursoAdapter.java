package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.InscricaoRequest;
import br.com.devquote.minicurso.dto.response.InscricaoResponse;
import br.com.devquote.minicurso.entity.InscricaoMinicurso;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InscricaoMinicursoAdapter {

    public static InscricaoResponse toResponseDTO(InscricaoMinicurso entity) {
        if (entity == null) {
            return null;
        }

        return InscricaoResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .curso(entity.getCurso())
                .periodo(entity.getPeriodo())
                .nivelProgramacao(entity.getNivelProgramacao())
                .expectativa(entity.getExpectativa())
                .confirmado(entity.getConfirmado())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static InscricaoMinicurso toEntity(InscricaoRequest dto) {
        if (dto == null) {
            return null;
        }

        return InscricaoMinicurso.builder()
                .nome(dto.getNome())
                .email(dto.getEmail().toLowerCase().trim())
                .telefone(dto.getTelefone())
                .curso(dto.getCurso())
                .periodo(dto.getPeriodo())
                .nivelProgramacao(dto.getNivelProgramacao())
                .expectativa(dto.getExpectativa())
                .build();
    }
}
