package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.ModuloEventoRequest;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.ModuloEvento;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ModuloEventoAdapter {

    public static ModuloEventoResponse toResponseDTO(ModuloEvento entity, List<ItemModuloResponse> itens) {
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
                .build();
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
