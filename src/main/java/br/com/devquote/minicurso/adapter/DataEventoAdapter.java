package br.com.devquote.minicurso.adapter;

import br.com.devquote.minicurso.dto.request.DataEventoRequest;
import br.com.devquote.minicurso.dto.response.DataEventoResponse;
import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import br.com.devquote.minicurso.entity.DataEvento;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@UtilityClass
public class DataEventoAdapter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

    public static DataEventoResponse toResponseDTO(DataEvento entity) {
        if (entity == null) {
            return null;
        }

        return DataEventoResponse.builder()
                .id(entity.getId())
                .dataEvento(entity.getDataEvento())
                .horarioInicio(entity.getHorarioInicio())
                .horarioFim(entity.getHorarioFim())
                .ordem(entity.getOrdem())
                .dataFormatada(formatarData(entity.getDataEvento()))
                .horarioFormatado(formatarHorario(entity.getHorarioInicio(), entity.getHorarioFim()))
                .build();
    }

    public static List<DataEventoResponse> toResponseDTOList(List<DataEvento> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DataEventoAdapter::toResponseDTO)
                .toList();
    }

    public static DataEvento toEntity(DataEventoRequest dto, ConfiguracaoEvento configuracaoEvento) {
        if (dto == null) {
            return null;
        }
        return DataEvento.builder()
                .configuracaoEvento(configuracaoEvento)
                .dataEvento(dto.getDataEvento())
                .horarioInicio(dto.getHorarioInicio())
                .horarioFim(dto.getHorarioFim())
                .ordem(dto.getOrdem())
                .build();
    }

    public static void updateEntityFromDto(DataEventoRequest dto, DataEvento entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setDataEvento(dto.getDataEvento());
        entity.setHorarioInicio(dto.getHorarioInicio());
        entity.setHorarioFim(dto.getHorarioFim());
        entity.setOrdem(dto.getOrdem());
    }

    private static String formatarData(LocalDate data) {
        if (data == null) {
            return null;
        }
        return data.format(DATE_FORMATTER);
    }

    private static String formatarHorario(LocalTime inicio, LocalTime fim) {
        if (inicio == null) {
            return null;
        }
        if (fim != null) {
            return inicio + " as " + fim;
        }
        return inicio.toString();
    }
}
