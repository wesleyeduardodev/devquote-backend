package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponseDTO;
import java.util.List;

public interface QuoteBillingMonthQuoteService {

    List<QuoteBillingMonthQuoteResponseDTO> findAll();
    QuoteBillingMonthQuoteResponseDTO findById(Long id);
    QuoteBillingMonthQuoteResponseDTO create(QuoteBillingMonthQuoteRequestDTO dto);
    QuoteBillingMonthQuoteResponseDTO update(Long id, QuoteBillingMonthQuoteRequestDTO dto);
    void delete(Long id);
}