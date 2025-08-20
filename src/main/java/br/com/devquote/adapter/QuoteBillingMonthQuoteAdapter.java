package br.com.devquote.adapter;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import br.com.devquote.entity.Quote;
import br.com.devquote.entity.QuoteBillingMonth;
import br.com.devquote.entity.QuoteBillingMonthQuote;
import lombok.experimental.UtilityClass;

@UtilityClass
public class QuoteBillingMonthQuoteAdapter {

    public static QuoteBillingMonthQuoteResponse toResponseDTO(QuoteBillingMonthQuote entity) {
        if (entity == null) return null;

        return QuoteBillingMonthQuoteResponse.builder()
                .id(entity.getId())
                .quoteBillingMonthId(entity.getQuoteBillingMonth().getId())
                .quoteId(entity.getQuote().getId())
                .taskName(entity.getQuote().getTask().getDescription())
                .taskCode(entity.getQuote().getTask().getCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static QuoteBillingMonthQuote toEntity(QuoteBillingMonthQuoteRequest dto, QuoteBillingMonth qbm, Quote quote) {
        if (dto == null) return null;

        return QuoteBillingMonthQuote.builder()
                .quoteBillingMonth(qbm)
                .quote(quote)
                .build();
    }

    public static void updateEntityFromDto(QuoteBillingMonthQuoteRequest dto, QuoteBillingMonthQuote entity,
                                           QuoteBillingMonth qbm, Quote quote) {
        if (dto == null || entity == null) return;
        entity.setQuoteBillingMonth(qbm);
        entity.setQuote(quote);
    }
}