package br.com.devquote.service.impl;
import br.com.devquote.adapter.QuoteBillingMonthQuoteAdapter;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponseDTO;
import br.com.devquote.entity.Quote;
import br.com.devquote.entity.QuoteBillingMonth;
import br.com.devquote.entity.QuoteBillingMonthQuote;
import br.com.devquote.repository.QuoteBillingMonthQuoteRepository;
import br.com.devquote.repository.QuoteBillingMonthRepository;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.service.QuoteBillingMonthQuoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteBillingMonthQuoteServiceImpl implements QuoteBillingMonthQuoteService {

    private final QuoteBillingMonthQuoteRepository repository;
    private final QuoteBillingMonthRepository quoteBillingMonthRepository;
    private final QuoteRepository quoteRepository;

    @Override
    public List<QuoteBillingMonthQuoteResponseDTO> findAll() {
        return repository.findAllOrderedById().stream()
                .map(QuoteBillingMonthQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteBillingMonthQuoteResponseDTO findById(Long id) {
        QuoteBillingMonthQuote entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonthQuote not found"));
        return QuoteBillingMonthQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthQuoteResponseDTO create(QuoteBillingMonthQuoteRequestDTO dto) {
        if (repository.existsByQuoteBillingMonth_IdAndQuote_Id(dto.getQuoteBillingMonthId(), dto.getQuoteId())) {
            throw new DataIntegrityViolationException("This quote is already linked to the given billing month.");
        }
        QuoteBillingMonth qbm = quoteBillingMonthRepository.findById(dto.getQuoteBillingMonthId())
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found"));
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        QuoteBillingMonthQuote entity = QuoteBillingMonthQuoteAdapter.toEntity(dto, qbm, quote);
        entity = repository.save(entity);
        return QuoteBillingMonthQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthQuoteResponseDTO update(Long id, QuoteBillingMonthQuoteRequestDTO dto) {
        QuoteBillingMonthQuote entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonthQuote not found"));

        QuoteBillingMonth qbm = quoteBillingMonthRepository.findById(dto.getQuoteBillingMonthId())
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found"));
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        QuoteBillingMonthQuoteAdapter.updateEntityFromDto(dto, entity, qbm, quote);
        entity = repository.save(entity);
        return QuoteBillingMonthQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<QuoteBillingMonthQuoteResponseDTO> findByQuoteBillingMonthId(Long billingMonthId) {
        return repository.findByQuoteBillingMonth_Id(billingMonthId)
                .stream()
                .map(QuoteBillingMonthQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }
}
