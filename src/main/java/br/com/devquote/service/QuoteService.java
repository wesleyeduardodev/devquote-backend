package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteRequest;
import br.com.devquote.dto.response.QuoteResponse;
import br.com.devquote.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface QuoteService {
    List<QuoteResponse> findAll();

    QuoteResponse findById(Long id);

    QuoteResponse create(QuoteRequest dto);

    QuoteResponse update(Long id, QuoteRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<QuoteResponse> findAllPaginated(Long id,
                                         Long taskId,
                                         String taskName,
                                         String taskCode,
                                         String status,
                                         String createdAt,
                                         String updatedAt,
                                         Pageable pageable);

    Quote findByTaskId(Long taskId);
}