package br.com.devquote.adapter;
import br.com.devquote.dto.request.QuoteBillingMonthRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthResponseDTO;
import br.com.devquote.entity.QuoteBillingMonth;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuoteBillingMonthAdapter {

    public static QuoteBillingMonthResponseDTO toResponseDTO(QuoteBillingMonth entity) {
        if (entity == null) {
            return null;
        }

        return QuoteBillingMonthResponseDTO.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .paymentDate(entity.getPaymentDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static QuoteBillingMonth toEntity(QuoteBillingMonthRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return QuoteBillingMonth.builder()
                .month(dto.getMonth())
                .year(dto.getYear())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus())
                .build();
    }

    public static void updateEntityFromDto(QuoteBillingMonthRequestDTO dto, QuoteBillingMonth entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setMonth(dto.getMonth());
        entity.setYear(dto.getYear());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setStatus(dto.getStatus());
    }
}
