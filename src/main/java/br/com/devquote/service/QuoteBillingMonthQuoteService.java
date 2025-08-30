package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
import br.com.devquote.entity.QuoteBillingMonthQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    
    // Novos métodos para paginação e operações em massa
    Page<QuoteBillingMonthQuoteResponse> findByQuoteBillingMonthIdPaginated(Long billingMonthId, Pageable pageable);
    List<QuoteBillingMonthQuoteResponse> bulkCreate(List<QuoteBillingMonthQuoteRequest> requests);
    void bulkUnlinkByBillingMonthAndQuoteIds(Long billingMonthId, List<Long> quoteIds);
    
    // Método para verificar se um quote está vinculado ao faturamento
    boolean existsByQuoteId(Long quoteId);
}