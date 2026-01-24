package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.InstrutorRequest;
import br.com.devquote.minicurso.dto.response.InstrutorResponse;
import br.com.devquote.minicurso.dto.response.ModuloSimplificadoResponse;
import br.com.devquote.minicurso.entity.InstrutorMinicurso;
import br.com.devquote.minicurso.entity.ModuloEvento;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class InstrutorMinicursoAdapter {

    public static InstrutorResponse toResponseDTO(InstrutorMinicurso entity) {
        if (entity == null) {
            return null;
        }

        List<ModuloSimplificadoResponse> modulos = toModulosSimplificados(entity.getModulos());

        return InstrutorResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .localTrabalho(entity.getLocalTrabalho())
                .tempoCarreira(entity.getTempoCarreira())
                .miniBio(entity.getMiniBio())
                .fotoUrl(entity.getFotoUrl())
                .email(entity.getEmail())
                .linkedin(entity.getLinkedin())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .modulos(modulos)
                .build();
    }

    public static InstrutorMinicurso toEntity(InstrutorRequest dto) {
        if (dto == null) {
            return null;
        }

        return InstrutorMinicurso.builder()
                .nome(dto.getNome())
                .localTrabalho(dto.getLocalTrabalho())
                .tempoCarreira(dto.getTempoCarreira())
                .miniBio(dto.getMiniBio())
                .email(dto.getEmail())
                .linkedin(dto.getLinkedin())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }

    public static void updateEntityFromDto(InstrutorRequest dto, InstrutorMinicurso entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setNome(dto.getNome());
        entity.setLocalTrabalho(dto.getLocalTrabalho());
        entity.setTempoCarreira(dto.getTempoCarreira());
        entity.setMiniBio(dto.getMiniBio());
        entity.setEmail(dto.getEmail());
        entity.setLinkedin(dto.getLinkedin());
        if (dto.getAtivo() != null) {
            entity.setAtivo(dto.getAtivo());
        }
    }

    public static List<ModuloSimplificadoResponse> toModulosSimplificados(Set<ModuloEvento> modulos) {
        if (modulos == null) {
            return Collections.emptyList();
        }

        List<ModuloEvento> modulosCopy;
        try {
            modulosCopy = new ArrayList<>(modulos);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        if (modulosCopy.isEmpty()) {
            return Collections.emptyList();
        }

        return modulosCopy.stream()
                .map(m -> ModuloSimplificadoResponse.builder()
                        .id(m.getId())
                        .titulo(m.getTitulo())
                        .build())
                .collect(Collectors.toList());
    }
}
