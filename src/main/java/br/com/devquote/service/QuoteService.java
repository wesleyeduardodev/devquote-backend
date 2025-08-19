package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface QuoteService {
    List<QuoteResponseDTO> findAll();

    QuoteResponseDTO findById(Long id);

    QuoteResponseDTO create(QuoteRequestDTO dto);

    QuoteResponseDTO update(Long id, QuoteRequestDTO dto);

    void delete(Long id);

    Page<QuoteResponseDTO> findAllPaginated(Long id,
                                            Long taskId,
                                            String taskName,
                                            String taskCode,
                                            String status,
                                            String createdAt,
                                            String updatedAt,
                                            Pageable pageable);
}