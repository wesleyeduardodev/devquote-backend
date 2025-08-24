package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.entity.QuoteBillingMonth;

import java.util.List;

public interface QuoteBillingMonthService {
    List<QuoteBillingMonthResponse> findAll();
    QuoteBillingMonthResponse findById(Long id);
    QuoteBillingMonthResponse create(QuoteBillingMonthRequest dto);
    QuoteBillingMonthResponse update(Long id, QuoteBillingMonthRequest dto);
    void delete(Long id);
    void deleteBulk(List<Long> ids);
    QuoteBillingMonth findByYearAndMonth(Integer year, Integer month);
}
