package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ConfiguracaoEventoAdapter {

    public static ConfiguracaoEventoResponse toResponseDTO(
            ConfiguracaoEvento entity,
            long totalInscritos,
            List<ModuloEventoResponse> modulos) {

        if (entity == null) {
            return null;
        }

        Integer vagasDisponiveis = null;
        if (entity.getQuantidadeVagas() != null) {
            vagasDisponiveis = Math.max(0, entity.getQuantidadeVagas() - (int) totalInscritos);
        }

        int cargaHorariaTotal = calcularCargaHorariaTotal(modulos);

        return ConfiguracaoEventoResponse.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .dataEvento(entity.getDataEvento())
                .horarioInicio(entity.getHorarioInicio())
                .horarioFim(entity.getHorarioFim())
                .local(entity.getLocal())
                .quantidadeVagas(entity.getQuantidadeVagas())
                .inscricoesAbertas(entity.getInscricoesAbertas())
                .exibirFaleConosco(entity.getExibirFaleConosco())
                .emailContato(entity.getEmailContato())
                .whatsappContato(entity.getWhatsappContato())
                .vagasDisponiveis(vagasDisponiveis)
                .cargaHorariaTotal(cargaHorariaTotal)
                .cargaHorariaTotalFormatada(ModuloEventoAdapter.formatarCargaHoraria(cargaHorariaTotal))
                .modulos(modulos)
                .build();
    }

    public static void updateEntityFromDto(ConfiguracaoEventoRequest dto, ConfiguracaoEvento entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setTitulo(dto.getTitulo());
        entity.setDataEvento(dto.getDataEvento());
        entity.setHorarioInicio(dto.getHorarioInicio());
        entity.setHorarioFim(dto.getHorarioFim());
        entity.setLocal(dto.getLocal());
        entity.setQuantidadeVagas(dto.getQuantidadeVagas());
        if (dto.getInscricoesAbertas() != null) {
            entity.setInscricoesAbertas(dto.getInscricoesAbertas());
        }
        if (dto.getExibirFaleConosco() != null) {
            entity.setExibirFaleConosco(dto.getExibirFaleConosco());
        }
        entity.setEmailContato(dto.getEmailContato());
        entity.setWhatsappContato(dto.getWhatsappContato());
    }

    private static int calcularCargaHorariaTotal(List<ModuloEventoResponse> modulos) {
        if (modulos == null || modulos.isEmpty()) {
            return 0;
        }

        return modulos.stream()
                .filter(m -> m.getCargaHoraria() != null)
                .mapToInt(ModuloEventoResponse::getCargaHoraria)
                .sum();
    }
}
