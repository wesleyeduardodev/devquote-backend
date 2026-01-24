package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.InstrutorSimplificadoResponse;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.InstrutorMinicurso;
import br.com.devquote.minicurso.entity.ModuloEvento;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ModuloEventoAdapter {

    public static ModuloEventoResponse toResponseDTO(ModuloEvento entity, List<ItemModuloResponse> itens) {
        return toResponseDTO(entity, itens, null);
    }

    public static ModuloEventoResponse toResponseDTO(ModuloEvento entity, List<ItemModuloResponse> itens, List<InstrutorSimplificadoResponse> instrutores) {
        if (entity == null) {
            return null;
        }

        return ModuloEventoResponse.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .descricao(entity.getDescricao())
                .ordem(entity.getOrdem())
                .cargaHoraria(entity.getCargaHoraria())
                .cargaHorariaFormatada(formatarCargaHoraria(entity.getCargaHoraria()))
                .ativo(entity.getAtivo())
                .itens(itens)
                .instrutores(instrutores)
                .build();
    }

    public static List<InstrutorSimplificadoResponse> toInstrutoresSimplificados(Set<InstrutorMinicurso> instrutores) {
        if (instrutores == null) {
            return Collections.emptyList();
        }

        List<InstrutorMinicurso> instrutoresCopy;
        try {
            instrutoresCopy = new ArrayList<>(instrutores);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        if (instrutoresCopy.isEmpty()) {
            return Collections.emptyList();
        }

        return instrutoresCopy.stream()
                .map(i -> InstrutorSimplificadoResponse.builder()
                        .id(i.getId())
                        .nome(i.getNome())
                        .build())
                .collect(Collectors.toList());
    }

    public static ModuloEvento toEntity(ModuloEventoRequest dto, ConfiguracaoEvento configuracaoEvento) {
        if (dto == null) {
            return null;
        }

        return ModuloEvento.builder()
                .configuracaoEvento(configuracaoEvento)
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .ordem(dto.getOrdem())
                .cargaHoraria(dto.getCargaHoraria())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }

    public static void updateEntityFromDto(ModuloEventoRequest dto, ModuloEvento entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setTitulo(dto.getTitulo());
        entity.setDescricao(dto.getDescricao());
        entity.setOrdem(dto.getOrdem());
        entity.setCargaHoraria(dto.getCargaHoraria());
        if (dto.getAtivo() != null) {
            entity.setAtivo(dto.getAtivo());
        }
    }

    public static String formatarCargaHoraria(Integer minutos) {
        if (minutos == null || minutos <= 0) {
            return null;
        }

        int horas = minutos / 60;
        int mins = minutos % 60;

        if (horas > 0 && mins > 0) {
            return horas + "h" + mins + "min";
        } else if (horas > 0) {
            return horas + "h";
        } else {
            return mins + "min";
        }
    }
}
