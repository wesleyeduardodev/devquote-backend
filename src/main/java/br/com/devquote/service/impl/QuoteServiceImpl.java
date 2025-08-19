package br.com.devquote.service.impl;

import br.com.devquote.adapter.QuoteAdapter;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import br.com.devquote.entity.Quote;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.QuoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<QuoteResponseDTO> findAll() {
        return quoteRepository.findAllOrderedById().stream()
                .map(QuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteResponseDTO findById(Long id) {
        Quote entity = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteResponseDTO create(QuoteRequestDTO dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Quote entity = QuoteAdapter.toEntity(dto, task);
        entity = quoteRepository.save(entity);
        return QuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteResponseDTO update(Long id, QuoteRequestDTO dto) {
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
    public Page<QuoteResponseDTO> findAllPaginated(Long id,
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
}
