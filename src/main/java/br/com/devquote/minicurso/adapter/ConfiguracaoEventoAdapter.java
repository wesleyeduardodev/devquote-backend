package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.ConfiguracaoEventoRequest;
import br.com.devquote.minicurso.dto.response.ConfiguracaoEventoResponse;
import br.com.devquote.minicurso.dto.response.DataEventoResponse;
import br.com.devquote.minicurso.dto.response.ModuloEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.DataEvento;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.List;

@UtilityClass
public class ConfiguracaoEventoAdapter {

    public static ConfiguracaoEventoResponse toResponseDTO(
            ConfiguracaoEvento entity,
            long totalInscritos,
            List<ModuloEventoResponse> modulos,
            List<DataEventoResponse> datasEvento) {

        if (entity == null) {
            return null;
        }

        Integer vagasDisponiveis = null;
        if (entity.getQuantidadeVagas() != null) {
            vagasDisponiveis = Math.max(0, entity.getQuantidadeVagas() - (int) totalInscritos);
        }

        int cargaHorariaTotal = calcularCargaHorariaTotalPorDatas(entity.getDatasEvento());

        return ConfiguracaoEventoResponse.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .datasEvento(datasEvento)
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

    private static int calcularCargaHorariaTotalPorDatas(List<DataEvento> datasEvento) {
        if (datasEvento == null || datasEvento.isEmpty()) {
            return 0;
        }

        return datasEvento.stream()
                .filter(d -> d.getHorarioInicio() != null && d.getHorarioFim() != null)
                .mapToInt(d -> (int) Duration.between(d.getHorarioInicio(), d.getHorarioFim()).toMinutes())
                .sum();
    }
}
