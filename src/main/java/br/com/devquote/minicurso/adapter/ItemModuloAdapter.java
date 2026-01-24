package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.ItemModuloRequest;
import br.com.devquote.minicurso.dto.response.ItemModuloResponse;
import br.com.devquote.minicurso.entity.ItemModulo;
import br.com.devquote.minicurso.entity.ModuloEvento;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemModuloAdapter {

    public static ItemModuloResponse toResponseDTO(ItemModulo entity) {
        if (entity == null) {
            return null;
        }

        return ItemModuloResponse.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .descricao(entity.getDescricao())
                .ordem(entity.getOrdem())
                .duracao(entity.getDuracao())
                .ativo(entity.getAtivo())
                .build();
    }

    public static ItemModulo toEntity(ItemModuloRequest dto, ModuloEvento modulo) {
        if (dto == null) {
            return null;
        }

        return ItemModulo.builder()
                .modulo(modulo)
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .ordem(dto.getOrdem())
                .duracao(dto.getDuracao())
                .ativo(dto.getAtivo() != null ? dto.getAtivo() : true)
                .build();
    }

    public static void updateEntityFromDto(ItemModuloRequest dto, ItemModulo entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setTitulo(dto.getTitulo());
        entity.setDescricao(dto.getDescricao());
        entity.setOrdem(dto.getOrdem());
        entity.setDuracao(dto.getDuracao());
        if (dto.getAtivo() != null) {
            entity.setAtivo(dto.getAtivo());
        }
    }
}
