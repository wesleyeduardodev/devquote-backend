package br.com.devquote.service.impl;

import br.com.devquote.adapter.QuoteBillingMonthAdapter;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.entity.QuoteBillingMonth;
import br.com.devquote.repository.QuoteBillingMonthRepository;
import br.com.devquote.service.QuoteBillingMonthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteBillingMonthServiceImpl implements QuoteBillingMonthService {

    private final QuoteBillingMonthRepository quoteBillingMonthRepository;

    @Override
    public List<QuoteBillingMonthResponse> findAll() {
        return quoteBillingMonthRepository.findAllOrderedById().stream()
                .map(QuoteBillingMonthAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteBillingMonthResponse findById(Long id) {
        QuoteBillingMonth entity = quoteBillingMonthRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found"));
        return QuoteBillingMonthAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthResponse create(QuoteBillingMonthRequest dto) {
        QuoteBillingMonth entity = QuoteBillingMonthAdapter.toEntity(dto);
        entity = quoteBillingMonthRepository.save(entity);
        return QuoteBillingMonthAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthResponse update(Long id, QuoteBillingMonthRequest dto) {
        QuoteBillingMonth entity = quoteBillingMonthRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found"));
        QuoteBillingMonthAdapter.updateEntityFromDto(dto, entity);
        entity = quoteBillingMonthRepository.save(entity);
        return QuoteBillingMonthAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        quoteBillingMonthRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        quoteBillingMonthRepository.deleteAllById(ids);
    }

    @Override
    public QuoteBillingMonth findByYearAndMonth(Integer year, Integer month) {
        Optional<QuoteBillingMonth> billingMonth = quoteBillingMonthRepository.findByYearAndMonth(year, month);
        return billingMonth.orElse(null);
    }

    @Override
    public Page<QuoteBillingMonthResponse> findAllPaginated(Integer month, Integer year, String status, Pageable pageable) {
        Page<QuoteBillingMonth> page = quoteBillingMonthRepository.findByOptionalFiltersPaginated(month, year, status, pageable);
        return page.map(QuoteBillingMonthAdapter::toResponseDTO);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Total de períodos
        long totalPeriods = quoteBillingMonthRepository.count();
        statistics.put("totalPeriods", totalPeriods);
        
        // Estatísticas por status
        List<Object[]> statusStats = quoteBillingMonthRepository.getStatusStatistics();
        Map<String, Long> statusCounts = new HashMap<>();
        
        for (Object[] stat : statusStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            statusCounts.put(status, count);
        }
        
        statistics.put("byStatus", statusCounts);
        
        // Anos únicos
        List<Integer> years = quoteBillingMonthRepository.findAll().stream()
            .map(QuoteBillingMonth::getYear)
            .distinct()
            .sorted((a, b) -> b.compareTo(a)) // Ordem decrescente
            .collect(Collectors.toList());
        
        statistics.put("availableYears", years);
        
        return statistics;
    }
}
