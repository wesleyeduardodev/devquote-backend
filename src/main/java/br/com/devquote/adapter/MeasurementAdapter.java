package br.com.devquote.adapter;
import br.com.devquote.dto.request.MeasurementRequestDTO;
import br.com.devquote.dto.response.MeasurementResponseDTO;
import br.com.devquote.entity.Measurement;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeasurementAdapter {

    public static MeasurementResponseDTO toResponseDTO(Measurement entity) {
        if (entity == null) {
            return null;
        }

        return MeasurementResponseDTO.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .paymentDate(entity.getPaymentDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Measurement toEntity(MeasurementRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Measurement.builder()
                .month(dto.getMonth())
                .year(dto.getYear())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus())
                .build();
    }

    public static void updateEntityFromDto(MeasurementRequestDTO dto, Measurement entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setMonth(dto.getMonth());
        entity.setYear(dto.getYear());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setStatus(dto.getStatus());
    }
}
