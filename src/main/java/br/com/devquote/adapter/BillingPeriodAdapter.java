package br.com.devquote.adapter;

import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.entity.BillingPeriod;

public class BillingPeriodAdapter {

    public static BillingPeriod toEntity(BillingPeriodRequest dto) {
        if (dto == null) {
            return null;
        }

        return BillingPeriod.builder()
                .month(dto.getMonth())
                .year(dto.getYear())
                .paymentDate(dto.getPaymentDate())
                .status(dto.getStatus())
                .build();
    }

    public static BillingPeriodResponse toResponseDTO(BillingPeriod entity) {
        if (entity == null) {
            return null;
        }

        return BillingPeriodResponse.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .paymentDate(entity.getPaymentDate())
                .status(entity.getStatus())
                .billingEmailSent(entity.getBillingEmailSent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static void updateEntityFromDto(BillingPeriodRequest dto, BillingPeriod entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setMonth(dto.getMonth());
        entity.setYear(dto.getYear());
        entity.setPaymentDate(dto.getPaymentDate());
        entity.setStatus(dto.getStatus());
    }
    
    public static BillingPeriod toEntity(BillingPeriodResponse response) {
        if (response == null) {
            return null;
        }

        return BillingPeriod.builder()
                .id(response.getId())
                .month(response.getMonth())
                .year(response.getYear())
                .paymentDate(response.getPaymentDate())
                .status(response.getStatus())
                .billingEmailSent(response.getBillingEmailSent())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}