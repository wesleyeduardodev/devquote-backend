package br.com.devquote.service.impl;

import br.com.devquote.adapter.BillingPeriodAdapter;
import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.repository.BillingPeriodRepository;
import br.com.devquote.service.BillingPeriodService;
import br.com.devquote.utils.ExcelReportUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BillingPeriodServiceImpl implements BillingPeriodService {

    private final BillingPeriodRepository billingPeriodRepository;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;

    @Override
    public List<BillingPeriodResponse> findAll() {
        return billingPeriodRepository.findAllOrderedById().stream()
                .map(BillingPeriodAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BillingPeriodResponse> findAllWithTotals() {
        String sql = """
            SELECT 
                bp.id,
                bp.month,
                bp.year,
                bp.payment_date,
                bp.status,
                bp.created_at,
                bp.updated_at,
                COALESCE(SUM(t.amount), 0) as total_amount
            FROM billing_period bp
            LEFT JOIN billing_period_task bpt ON bp.id = bpt.billing_period_id
            LEFT JOIN task t ON bpt.task_id = t.id
            GROUP BY bp.id, bp.month, bp.year, bp.payment_date, bp.status, bp.created_at, bp.updated_at
            ORDER BY bp.id DESC
        """;
        
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream().map(row -> {
            return BillingPeriodResponse.builder()
                .id(((Number) row[0]).longValue())
                .month((Integer) row[1])
                .year((Integer) row[2])
                .paymentDate(row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null)
                .status((String) row[4])
                .createdAt(row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null)
                .updatedAt(row[6] != null ? ((java.sql.Timestamp) row[6]).toLocalDateTime() : null)
                .totalAmount(new java.math.BigDecimal(row[7].toString()))
                .build();
        }).collect(Collectors.toList());
    }

    @Override
    public BillingPeriodResponse findById(Long id) {
        BillingPeriod entity = billingPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        return BillingPeriodAdapter.toResponseDTO(entity);
    }

    @Override
    public BillingPeriodResponse create(BillingPeriodRequest dto) {
        BillingPeriod entity = BillingPeriodAdapter.toEntity(dto);
        entity = billingPeriodRepository.save(entity);
        return BillingPeriodAdapter.toResponseDTO(entity);
    }

    @Override
    public BillingPeriodResponse update(Long id, BillingPeriodRequest dto) {
        BillingPeriod entity = billingPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        BillingPeriodAdapter.updateEntityFromDto(dto, entity);
        entity = billingPeriodRepository.save(entity);
        return BillingPeriodAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        billingPeriodRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        billingPeriodRepository.deleteAllById(ids);
    }

    @Override
    public BillingPeriod findByYearAndMonth(Integer year, Integer month) {
        Optional<BillingPeriod> billingMonth = billingPeriodRepository.findByYearAndMonth(year, month);
        return billingMonth.orElse(null);
    }

    @Override
    public Page<BillingPeriodResponse> findAllPaginated(Integer month, Integer year, String status, Pageable pageable) {
        Page<BillingPeriod> page = billingPeriodRepository.findByOptionalFiltersPaginated(month, year, status, pageable);
        return page.map(BillingPeriodAdapter::toResponseDTO);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalPeriods = billingPeriodRepository.count();
        statistics.put("totalPeriods", totalPeriods);
        
        List<Object[]> statusStats = billingPeriodRepository.getStatusStatistics();
        Map<String, Long> statusCounts = new HashMap<>();
        
        for (Object[] stat : statusStats) {
            String status = (String) stat[0];
            Long count = (Long) stat[1];
            statusCounts.put(status, count);
        }
        
        statistics.put("byStatus", statusCounts);
        
        List<Integer> years = billingPeriodRepository.findAll().stream()
            .map(BillingPeriod::getYear)
            .distinct()
            .sorted((a, b) -> b.compareTo(a))
            .collect(Collectors.toList());
        
        statistics.put("availableYears", years);
        
        return statistics;
    }

    @Override
    public byte[] exportToExcel(Integer month, Integer year, String status) throws IOException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT 
                bp.year as billing_year,
                bp.month as billing_month,
                bp.status as billing_status,
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.status as task_status,
                t.amount as task_amount,
                (SELECT COUNT(*) FROM sub_task st WHERE st.task_id = t.id) as subtasks_count,
                r.name as requester_name,
                bp.created_at,
                bp.updated_at
            FROM billing_period bp
            INNER JOIN billing_period_task bpt ON bpt.billing_period_id = bp.id
            INNER JOIN task t ON bpt.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            WHERE 1=1
        """);

        if (month != null) {
            sqlBuilder.append(" AND bp.month = ?1");
        }
        if (year != null) {
            sqlBuilder.append(" AND bp.year = ?").append(month != null ? "2" : "1");
        }
        if (status != null && !status.trim().isEmpty()) {
            int paramIndex = (month != null ? 1 : 0) + (year != null ? 1 : 0) + 1;
            sqlBuilder.append(" AND bp.status = ?").append(paramIndex);
        }
        
        sqlBuilder.append(" ORDER BY bp.year DESC, bp.month DESC, t.id DESC");

        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        
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
            map.put("billing_year", row[0]);
            map.put("billing_month", row[1]);
            map.put("billing_status", row[2]);
            map.put("task_id", row[3]);
            map.put("task_code", row[4]);
            map.put("task_title", row[5]);
            map.put("task_status", row[6]);
            map.put("task_amount", row[7]);
            map.put("subtasks_count", row[8]);
            map.put("requester_name", row[9]);
            map.put("created_at", row[10]);
            map.put("updated_at", row[11]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateBillingReport(data);
    }

    @Override
    public void deleteWithAllLinkedTasks(Long id) {
        log.info("BILLING_PERIOD DELETE_WITH_TASKS id={}", id);
        
        // Verificar se o período existe
        BillingPeriod billingPeriod = billingPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing period not found with id: " + id));
        
        try {
            // 1. Primeiro, remover todos os vínculos de tarefas
            String deleteTaskLinksQuery = "DELETE FROM billing_period_task WHERE billing_period_id = :billingPeriodId";
            Query deleteTaskLinksNativeQuery = entityManager.createNativeQuery(deleteTaskLinksQuery);
            deleteTaskLinksNativeQuery.setParameter("billingPeriodId", id);
            int deletedLinks = deleteTaskLinksNativeQuery.executeUpdate();
            
            log.info("BILLING_PERIOD DELETE_WITH_TASKS id={} deletedTaskLinks={}", id, deletedLinks);
            
            // 2. Depois, remover o período de faturamento
            billingPeriodRepository.deleteById(id);
            
            log.info("BILLING_PERIOD DELETE_WITH_TASKS id={} - completed successfully", id);
            
        } catch (Exception e) {
            log.error("BILLING_PERIOD DELETE_WITH_TASKS id={} - error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro ao excluir período de faturamento com tarefas vinculadas: " + e.getMessage());
        }
    }
}