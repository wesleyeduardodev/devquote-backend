package br.com.devquote.adapter;
import br.com.devquote.dto.request.MeasurementQuoteRequestDTO;
import br.com.devquote.dto.response.MeasurementQuoteResponseDTO;
import br.com.devquote.entity.Measurement;
import br.com.devquote.entity.MeasurementQuote;
import br.com.devquote.entity.Quote;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeasurementQuoteAdapter {

    public static MeasurementQuoteResponseDTO toResponseDTO(MeasurementQuote entity) {
        if (entity == null) {
            return null;
        }

        return MeasurementQuoteResponseDTO.builder()
                .id(entity.getId())
                .measurementId(entity.getMeasurement() != null ? entity.getMeasurement().getId() : null)
                .quoteId(entity.getQuote() != null ? entity.getQuote().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static MeasurementQuote toEntity(MeasurementQuoteRequestDTO dto, Measurement measurement, Quote quote) {
        if (dto == null) {
            return null;
        }

        return MeasurementQuote.builder()
                .measurement(measurement)
                .quote(quote)
                .build();
    }

    public static void updateEntityFromDto(MeasurementQuoteRequestDTO dto, MeasurementQuote entity, Measurement measurement, Quote quote) {
        if (dto == null || entity == null) {
            return;
        }

        if (measurement != null) {
            entity.setMeasurement(measurement);
        }

        if (quote != null) {
            entity.setQuote(quote);
        }
    }
}
