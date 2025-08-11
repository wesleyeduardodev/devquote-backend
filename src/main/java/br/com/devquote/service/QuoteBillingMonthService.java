package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthResponseDTO;
import java.util.List;

public interface QuoteBillingMonthService {
    List<QuoteBillingMonthResponseDTO> findAll();
    QuoteBillingMonthResponseDTO findById(Long id);
    QuoteBillingMonthResponseDTO create(QuoteBillingMonthRequestDTO dto);
    QuoteBillingMonthResponseDTO update(Long id, QuoteBillingMonthRequestDTO dto);
    void delete(Long id);
}
