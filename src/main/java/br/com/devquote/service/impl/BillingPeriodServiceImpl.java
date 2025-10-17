package br.com.devquote.service.impl;

import br.com.devquote.adapter.BillingPeriodAdapter;
import br.com.devquote.dto.request.BillingPeriodRequest;
import br.com.devquote.dto.response.BillingPeriodResponse;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.repository.BillingPeriodRepository;
import br.com.devquote.repository.BillingPeriodTaskRepository;
import br.com.devquote.service.BillingPeriodService;
import br.com.devquote.service.BillingPeriodAttachmentService;
import br.com.devquote.service.EmailService;
import br.com.devquote.service.storage.FileStorageStrategy;
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
    private final BillingPeriodTaskRepository billingPeriodTaskRepository;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;
    private final EmailService emailService;
    private final BillingPeriodAttachmentService billingPeriodAttachmentService;
    private final FileStorageStrategy fileStorageStrategy;

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
                bp.billing_email_sent,
                bp.created_at,
                bp.updated_at,
                COALESCE(SUM(t.amount), 0) as total_amount,
                COUNT(t.id) as task_count
            FROM billing_period bp
            LEFT JOIN billing_period_task bpt ON bp.id = bpt.billing_period_id
            LEFT JOIN task t ON bpt.task_id = t.id
            GROUP BY bp.id, bp.month, bp.year, bp.payment_date, bp.status, bp.billing_email_sent, bp.created_at, bp.updated_at
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
                .billingEmailSent(row[5] != null ? (Boolean) row[5] : false)
                .createdAt(row[6] != null ? ((java.sql.Timestamp) row[6]).toLocalDateTime() : null)
                .updatedAt(row[7] != null ? ((java.sql.Timestamp) row[7]).toLocalDateTime() : null)
                .totalAmount(new java.math.BigDecimal(row[8].toString()))
                .taskCount(((Number) row[9]).longValue())
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
        // Buscar o período antes de excluir para o email
        BillingPeriod billingPeriod = billingPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        
        // ESTRATÉGIA ROBUSTA: Tentar baixar anexos para memória
        Map<String, byte[]> attachmentDataMap = new HashMap<>();
        boolean hasAttachments = false;
        
        try {
            List<br.com.devquote.entity.BillingPeriodAttachment> attachments = billingPeriodAttachmentService.getBillingPeriodAttachmentsEntities(id);
            if (attachments != null && !attachments.isEmpty()) {
                hasAttachments = true;
                for (br.com.devquote.entity.BillingPeriodAttachment attachment : attachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                            
                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put(attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download billing period attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing billing period attachments from database: {}", e.getMessage());
        }
        
        // STEP 2/3 - ENVIAR EMAIL (com anexos se conseguiu baixar, sem anexos se não conseguiu)
        try {
            if (attachmentDataMap.isEmpty() && hasAttachments) {
                // Tinha anexos mas não conseguiu baixar nenhum - enviar email simples
                emailService.sendBillingPeriodDeletedNotification(billingPeriod);
            } else if (!attachmentDataMap.isEmpty()) {
                // Conseguiu baixar anexos - enviar email com anexos
                emailService.sendBillingPeriodDeletedNotificationWithAttachmentData(billingPeriod, attachmentDataMap);
            } else {
                // Não tinha anexos - enviar email simples
                emailService.sendBillingPeriodDeletedNotification(billingPeriod);
            }
        } catch (Exception e) {
            log.error("FAILED to send deletion email for billing period ID: {} - Error: {}", id, e.getMessage());
            // Não impede a exclusão do período
        }
        
        // STEP 3/3 - DELETAR PERÍODO E RELACIONAMENTOS (sempre acontece, mesmo se email falhou)
        try {
            // 1. Excluir anexos (arquivos S3 e registros billing_period_attachments)
            billingPeriodAttachmentService.deleteAllBillingPeriodAttachmentsAndFolder(id);
            log.debug("Deleted all attachments for billing period ID: {}", id);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to delete attachments for billing period {}: {}", id, e.getMessage());
            throw e; // PROPAGAR ERRO - é crítico para integridade dos dados
        }
        
        try {
            // 2. Excluir relacionamentos com tarefas (billing_period_task)
            billingPeriodTaskRepository.deleteByBillingPeriodId(id);
            log.debug("Deleted all BillingPeriodTask records for billing period ID: {}", id);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to delete billing period tasks for period {}: {}", id, e.getMessage());
            throw e; // PROPAGAR ERRO - é crítico para integridade dos dados
        }
        
        // 3. Excluir o período de faturamento
        billingPeriodRepository.deleteById(id);
        log.debug("Successfully deleted BillingPeriod with ID: {}", id);
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
            map.put("task_amount", row[6]);
            map.put("subtasks_count", row[7]);
            map.put("requester_name", row[8]);
            map.put("created_at", row[9]);
            map.put("updated_at", row[10]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateBillingReport(data);
    }

    @Override
    public void deleteWithAllLinkedTasks(Long id) {
        log.debug("BILLING_PERIOD DELETE_WITH_TASKS id={}", id);
        
        // Usar o método delete() que já implementa o padrão robusto completo
        // (busca dados, envia email, exclui anexos, exclui relacionamentos, exclui período)
        delete(id);
        
        log.debug("BILLING_PERIOD DELETE_WITH_TASKS id={} - completed successfully using robust deletion pattern", id);
    }

    @Override
    public BillingPeriodResponse updateStatus(Long id, String status) {
        log.debug("BILLING_PERIOD UPDATE_STATUS id={} status={}", id, status);
        
        BillingPeriod entity = billingPeriodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        
        entity.setStatus(status);
        entity = billingPeriodRepository.save(entity);
        
        log.debug("BILLING_PERIOD UPDATE_STATUS id={} - completed successfully", id);
        
        return BillingPeriodAdapter.toResponseDTO(entity);
    }

    @Override
    public void sendBillingEmail(Long billingPeriodId, List<String> additionalEmails) {
        log.debug("BILLING_PERIOD SEND_EMAIL id={}", billingPeriodId);

        // Buscar o período de faturamento
        BillingPeriod billingPeriod = billingPeriodRepository.findById(billingPeriodId)
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found with id: " + billingPeriodId));

        // ESTRATÉGIA ROBUSTA: Tentar baixar anexos, se conseguir incluir no email
        Map<String, byte[]> attachmentDataMap = new HashMap<>();
        boolean hasAttachments = false;

        try {
            List<br.com.devquote.entity.BillingPeriodAttachment> attachments = billingPeriodAttachmentService.getBillingPeriodAttachmentsEntities(billingPeriodId);
            if (attachments != null && !attachments.isEmpty()) {
                hasAttachments = true;
                for (br.com.devquote.entity.BillingPeriodAttachment attachment : attachments) {
                    try {
                        try (java.io.InputStream inputStream = fileStorageStrategy.getFileStream(attachment.getFilePath());
                             java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {

                            inputStream.transferTo(baos);
                            byte[] data = baos.toByteArray();
                            attachmentDataMap.put(attachment.getOriginalFileName(), data);
                        }
                    } catch (Exception e) {
                        log.error("Failed to download billing period attachment {} from S3: {}", attachment.getOriginalFileName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error accessing billing period attachments from database: {}", e.getMessage());
        }

        try {
            // Enviar email com informações do período de faturamento (com anexos se conseguiu baixar)
            if (!attachmentDataMap.isEmpty()) {
                // Conseguiu baixar anexos - enviar email com anexos
                emailService.sendBillingPeriodNotificationWithAttachmentData(billingPeriod, attachmentDataMap, additionalEmails);
            } else {
                // Não conseguiu baixar anexos ou não tinha anexos - enviar email simples
                emailService.sendBillingPeriodNotificationAsync(billingPeriod, additionalEmails);
            }
            
            // Marcar como email enviado
            billingPeriod.setBillingEmailSent(true);
            billingPeriodRepository.save(billingPeriod);
            
            log.debug("BILLING_PERIOD SEND_EMAIL id={} - completed successfully", billingPeriodId);
            
        } catch (Exception e) {
            log.error("BILLING_PERIOD SEND_EMAIL id={} - error: {}", billingPeriodId, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar email de faturamento: " + e.getMessage());
        }
    }
}