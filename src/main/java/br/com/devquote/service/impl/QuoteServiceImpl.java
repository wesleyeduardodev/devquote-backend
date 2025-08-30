package br.com.devquote.service.impl;
import br.com.devquote.adapter.QuoteAdapter;
import br.com.devquote.dto.request.QuoteRequest;
import br.com.devquote.dto.response.QuoteResponse;
import br.com.devquote.entity.Quote;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.QuoteService;
import br.com.devquote.utils.ExcelReportUtils;
import br.com.devquote.utils.SecurityUtils;
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
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final TaskRepository taskRepository;
    private final EntityManager entityManager;
    private final ExcelReportUtils excelReportUtils;

    @Override
    public List<QuoteResponse> findAll() {
        return quoteRepository.findAllOrderedById().stream()
                .map(QuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteResponse findById(Long id) {
        Quote entity = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteResponse create(QuoteRequest dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Quote entity = QuoteAdapter.toEntity(dto, task);
        entity = quoteRepository.save(entity);
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteResponse update(Long id, QuoteRequest dto) {
        Quote entity = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        QuoteAdapter.updateEntityFromDto(dto, entity, task);
        entity = quoteRepository.save(entity);
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        quoteRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        quoteRepository.deleteAllById(ids);
    }

    @Override
    public Page<QuoteResponse> findAllPaginated(Long id,
                                                Long taskId,
                                                String taskName,
                                                String taskCode,
                                                String status,
                                                String createdAt,
                                                String updatedAt,
                                                Pageable pageable) {

        Page<Quote> page = quoteRepository.findByOptionalFieldsPaginated(
                id, taskId, taskName, taskCode, status, createdAt, updatedAt, pageable
        );

        return page.map(QuoteAdapter::toResponseDTO);
    }

    @Override
    public Quote findByTaskId(Long taskId){
        Optional<Quote> quote = quoteRepository.findByTaskId(taskId);
        return quote.orElse(null);
    }

    @Override
    public QuoteResponse updateStatus(Long id, String status) {
        Quote entity = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        entity.setStatus(status);
        entity = quoteRepository.save(entity);
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public byte[] exportToExcel() throws IOException {
        String sql = """
            SELECT 
                q.id as quote_id,
                q.status as quote_status,
                t.id as task_id,
                t.code as task_code,
                t.title as task_title,
                t.amount as task_amount,
                t.status as task_status,
                t.priority as task_priority,
                r.name as requester_name,
                CASE 
                    WHEN qbmq.id IS NOT NULL THEN 'Sim'
                    ELSE 'Não'
                END as has_billing,
                q.total_amount as quote_total_amount,
                q.created_at as quote_created_at,
                q.updated_at as quote_updated_at
            FROM quote q
            INNER JOIN task t ON q.task_id = t.id
            INNER JOIN requester r ON t.requester_id = r.id
            LEFT JOIN quote_billing_month_quote qbmq ON q.id = qbmq.quote_id
            ORDER BY q.id DESC
        """;

        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("quote_id", row[0]);
            map.put("quote_status", row[1]);
            map.put("task_id", row[2]);
            map.put("task_code", row[3]);
            map.put("task_title", row[4]);
            map.put("task_amount", row[5]);
            map.put("task_status", row[6]);
            map.put("task_priority", row[7]);
            map.put("requester_name", row[8]);
            map.put("has_billing", row[9]);
            map.put("quote_total_amount", row[10]);
            map.put("quote_created_at", row[11]);
            map.put("quote_updated_at", row[12]);
            return map;
        }).collect(Collectors.toList());

        return excelReportUtils.generateQuotesReport(data, true);
    }
}
