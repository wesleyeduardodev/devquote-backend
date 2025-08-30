package br.com.devquote.service.impl;

import br.com.devquote.adapter.QuoteBillingMonthAdapter;
import br.com.devquote.dto.request.QuoteBillingMonthRequest;
import br.com.devquote.dto.response.QuoteBillingMonthResponse;
import br.com.devquote.entity.QuoteBillingMonth;
import br.com.devquote.repository.QuoteBillingMonthRepository;
import br.com.devquote.service.QuoteBillingMonthService;
import br.com.devquote.utils.ExcelReportUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;

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

    @Override
    public byte[] exportToExcel(Integer month, Integer year, String status) throws IOException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT 
                qbm.year as billing_year,
                qbm.month as billing_month,
                qbm.status as billing_status,
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.status as task_status,
                t.amount as task_amount,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                q.id as quote_id,
                q.status as quote_status,
                q.total_amount as quote_amount,
                qbm.created_at,
                qbm.updated_at
            FROM quote_billing_month qbm
            INNER JOIN quote_billing_month_quote qbmq ON qbmq.quote_billing_month_id = qbm.id
            INNER JOIN quote q ON qbmq.quote_id = q.id
            INNER JOIN task t ON q.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            WHERE 1=1
        """);

        // Adicionar filtros dinamicamente
        if (month != null) {
            sqlBuilder.append(" AND qbm.month = ?1");
        }
        if (year != null) {
            sqlBuilder.append(" AND qbm.year = ?").append(month != null ? "2" : "1");
        }
        if (status != null && !status.trim().isEmpty()) {
            int paramIndex = (month != null ? 1 : 0) + (year != null ? 1 : 0) + 1;
            sqlBuilder.append(" AND qbm.status = ?").append(paramIndex);
        }
        
        sqlBuilder.append(" ORDER BY qbm.year DESC, qbm.month DESC, t.id DESC");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        
        // Definir parâmetros em ordem
        int paramIndex = 1;
        if (month != null) {
            query.setParameter(paramIndex++, month);
        }
        if (year != null) {
            query.setParameter(paramIndex++, year);
        }
        if (status != null && !status.trim().isEmpty()) {
            query.setParameter(paramIndex, status);
        }
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            // Dados do faturamento
            map.put("billing_year", row[0]);
            map.put("billing_month", row[1]);
            map.put("billing_status", row[2]);
            // Dados da tarefa
            map.put("task_id", row[3]);
            map.put("task_code", row[4]);
            map.put("task_title", row[5]);
            map.put("task_status", row[6]);
            map.put("task_amount", row[7]);
            map.put("subtasks_count", row[8]);
            map.put("requester_name", row[9]);
            // Dados do orçamento
            map.put("quote_id", row[10]);
            map.put("quote_status", row[11]);
            map.put("quote_amount", row[12]);
            // Datas
            map.put("created_at", row[13]);
            map.put("updated_at", row[14]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateBillingReport(data);
    }
}
