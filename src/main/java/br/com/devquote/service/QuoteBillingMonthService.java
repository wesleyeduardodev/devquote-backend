package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.entity.QuoteBillingMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface QuoteBillingMonthService {
    List<QuoteBillingMonthResponse> findAll();
    List<QuoteBillingMonthResponse> findAllWithTotals(); // Novo método com totais calculados
    QuoteBillingMonthResponse findById(Long id);
    QuoteBillingMonthResponse create(QuoteBillingMonthRequest dto);
    QuoteBillingMonthResponse update(Long id, QuoteBillingMonthRequest dto);
    void delete(Long id);
    void deleteBulk(List<Long> ids);
    QuoteBillingMonth findByYearAndMonth(Integer year, Integer month);
    
    // Novos métodos para paginação e estatísticas
    Page<QuoteBillingMonthResponse> findAllPaginated(Integer month, Integer year, String status, Pageable pageable);
    Map<String, Object> getStatistics();
    
    // Método para exportar relatório
    byte[] exportToExcel(Integer month, Integer year, String status) throws IOException;
}
