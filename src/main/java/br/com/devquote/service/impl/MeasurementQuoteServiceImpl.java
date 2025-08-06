package br.com.devquote.service.impl;
import br.com.devquote.adapter.MeasurementQuoteAdapter;
import br.com.devquote.dto.request.MeasurementQuoteRequestDTO;
import br.com.devquote.dto.response.MeasurementQuoteResponseDTO;
import br.com.devquote.entity.*;
import br.com.devquote.repository.*;
import br.com.devquote.service.MeasurementQuoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MeasurementQuoteServiceImpl implements MeasurementQuoteService {

    private final MeasurementQuoteRepository measurementQuoteRepository;
    private final MeasurementRepository measurementRepository;
    private final QuoteRepository quoteRepository;

    @Override
    public List<MeasurementQuoteResponseDTO> findAll() {
        return measurementQuoteRepository.findAll().stream()
                .map(MeasurementQuoteAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MeasurementQuoteResponseDTO findById(Long id) {
        MeasurementQuote entity = measurementQuoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MeasurementQuote not found"));
        return MeasurementQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public MeasurementQuoteResponseDTO create(MeasurementQuoteRequestDTO dto) {
        Measurement measurement = measurementRepository.findById(dto.getMeasurementId())
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        MeasurementQuote entity = MeasurementQuoteAdapter.toEntity(dto, measurement, quote);
        entity = measurementQuoteRepository.save(entity);
        return MeasurementQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public MeasurementQuoteResponseDTO update(Long id, MeasurementQuoteRequestDTO dto) {
        MeasurementQuote entity = measurementQuoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MeasurementQuote not found"));
        Measurement measurement = measurementRepository.findById(dto.getMeasurementId())
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        MeasurementQuoteAdapter.updateEntityFromDto(dto, entity, measurement, quote);
        entity = measurementQuoteRepository.save(entity);
        return MeasurementQuoteAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        measurementQuoteRepository.deleteById(id);
    }
}