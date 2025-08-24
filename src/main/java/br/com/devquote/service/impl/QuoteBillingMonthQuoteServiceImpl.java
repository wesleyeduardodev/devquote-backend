package br.com.devquote.service.impl;
import br.com.devquote.adapter.QuoteBillingMonthQuoteAdapter;
import br.com.devquote.dto.request.QuoteBillingMonthQuoteRequest;
import br.com.devquote.dto.response.QuoteBillingMonthQuoteResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteBillingMonthQuoteServiceImpl implements QuoteBillingMonthQuoteService {

    private final QuoteBillingMonthQuoteRepository repository;
    private final QuoteBillingMonthRepository quoteBillingMonthRepository;
    private final QuoteRepository quoteRepository;

    @Override
    public List<QuoteBillingMonthQuoteResponse> findAll() {
        return repository.findAllOrderedById().stream()
                .map(QuoteBillingMonthQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteBillingMonthQuoteResponse findById(Long id) {
        QuoteBillingMonthQuote entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonthQuote not found"));
        return QuoteBillingMonthQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthQuoteResponse create(QuoteBillingMonthQuoteRequest dto) {
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
    public QuoteBillingMonthQuoteResponse update(Long id, QuoteBillingMonthQuoteRequest dto) {
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
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        repository.deleteAllById(ids);
    }

    @Override
    public List<QuoteBillingMonthQuoteResponse> findByQuoteBillingMonthId(Long billingMonthId) {
        return repository.findByQuoteBillingMonth_Id(billingMonthId)
                .stream()
                .map(QuoteBillingMonthQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteBillingMonthQuote findByQuoteBillingMonthIdAndQuoteId(Long billingMonthId, Long quoteId){
        Optional<QuoteBillingMonthQuote> quote = repository.findByQuoteBillingMonthIdAndQuoteId(billingMonthId, quoteId);
        return quote.orElse(null);
    }

    @Override
    public Page<QuoteBillingMonthQuoteResponse> findByQuoteBillingMonthIdPaginated(Long billingMonthId, Pageable pageable) {
        Page<QuoteBillingMonthQuote> page = repository.findByQuoteBillingMonthIdPaginated(billingMonthId, pageable);
        return page.map(QuoteBillingMonthQuoteAdapter::toResponseDTO);
    }

    @Override
    public List<QuoteBillingMonthQuoteResponse> bulkCreate(List<QuoteBillingMonthQuoteRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<QuoteBillingMonthQuote> entities = new ArrayList<>();
        
        for (QuoteBillingMonthQuoteRequest dto : requests) {
            // Verifica se já existe
            if (repository.existsByQuoteBillingMonth_IdAndQuote_Id(dto.getQuoteBillingMonthId(), dto.getQuoteId())) {
                continue; // Pula se já existir
            }
            
            QuoteBillingMonth qbm = quoteBillingMonthRepository.findById(dto.getQuoteBillingMonthId())
                    .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found: " + dto.getQuoteBillingMonthId()));
            Quote quote = quoteRepository.findById(dto.getQuoteId())
                    .orElseThrow(() -> new RuntimeException("Quote not found: " + dto.getQuoteId()));

            QuoteBillingMonthQuote entity = QuoteBillingMonthQuoteAdapter.toEntity(dto, qbm, quote);
            entities.add(entity);
        }
        
        List<QuoteBillingMonthQuote> savedEntities = repository.saveAll(entities);
        
        return savedEntities.stream()
                .map(QuoteBillingMonthQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkUnlinkByBillingMonthAndQuoteIds(Long billingMonthId, List<Long> quoteIds) {
        if (quoteIds == null || quoteIds.isEmpty()) {
            return;
        }
        repository.deleteByQuoteBillingMonthIdAndQuoteIdIn(billingMonthId, quoteIds);
    }
}
