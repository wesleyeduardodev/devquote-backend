package br.com.devquote.service;
import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.entity.BillingPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BillingPeriodService {
    List<BillingPeriodResponse> findAll();
    List<BillingPeriodResponse> findAllWithTotals();
    BillingPeriodResponse findById(Long id);
    BillingPeriodResponse create(BillingPeriodRequest dto);
    BillingPeriodResponse update(Long id, BillingPeriodRequest dto);
    void delete(Long id);
    void deleteBulk(List<Long> ids);
    BillingPeriod findByYearAndMonth(Integer year, Integer month);
    
    Page<BillingPeriodResponse> findAllPaginated(Integer month, Integer year, String status, Pageable pageable);
    Map<String, Object> getStatistics();
    
    byte[] exportToExcel(Integer month, Integer year, String status) throws IOException;
    
    void deleteWithAllLinkedTasks(Long id);
}