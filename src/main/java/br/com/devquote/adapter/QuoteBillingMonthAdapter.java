package br.com.devquote.adapter;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.entity.QuoteBillingMonth;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuoteBillingMonthAdapter {

    public static QuoteBillingMonthResponse toResponseDTO(QuoteBillingMonth entity) {
        if (entity == null) {
            return null;
        }

        return QuoteBillingMonthResponse.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .paymentDate(entity.getPaymentDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static QuoteBillingMonth toEntity(QuoteBillingMonthRequest dto) {
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

    public static QuoteBillingMonth toEntity(QuoteBillingMonthResponse dto) {
        if (dto == null) {
            return null;
        }
        return QuoteBillingMonth.builder()
                .id(dto.getId())
                .month(dto.getMonth())
                .year(dto.getYear())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }


    public static void updateEntityFromDto(QuoteBillingMonthRequest dto, QuoteBillingMonth entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setMonth(dto.getMonth());
        entity.setYear(dto.getYear());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setStatus(dto.getStatus());
    }
}
