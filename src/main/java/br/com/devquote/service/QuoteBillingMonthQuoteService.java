package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import br.com.devquote.entity.QuoteBillingMonthQuote;

import java.util.List;

public interface QuoteBillingMonthQuoteService {

    List<QuoteBillingMonthQuoteResponse> findAll();
    QuoteBillingMonthQuoteResponse findById(Long id);
    QuoteBillingMonthQuoteResponse create(QuoteBillingMonthQuoteRequest dto);
    QuoteBillingMonthQuoteResponse update(Long id, QuoteBillingMonthQuoteRequest dto);
    void delete(Long id);
    void deleteBulk(List<Long> ids);
    List<QuoteBillingMonthQuoteResponse> findByQuoteBillingMonthId(Long billingMonthId);
    QuoteBillingMonthQuote findByQuoteBillingMonthIdAndQuoteId(Long billingMonthId, Long quoteId);
}