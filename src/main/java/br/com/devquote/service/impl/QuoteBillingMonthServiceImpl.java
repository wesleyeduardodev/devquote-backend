package br.com.devquote.service.impl;
import br.com.devquote.adapter.QuoteBillingMonthAdapter;
import br.com.devquote.dto.request.QuoteBillingMonthRequestDTO;
import br.com.devquote.dto.response.QuoteBillingMonthResponseDTO;
import br.com.devquote.entity.QuoteBillingMonth;
import br.com.devquote.repository.QuoteBillingMonthRepository;
import br.com.devquote.service.QuoteBillingMonthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuoteBillingMonthServiceImpl implements QuoteBillingMonthService {

    private final QuoteBillingMonthRepository quoteBillingMonthRepository;

    @Override
    public List<QuoteBillingMonthResponseDTO> findAll() {
        return quoteBillingMonthRepository.findAll().stream()
                .map(QuoteBillingMonthAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuoteBillingMonthResponseDTO findById(Long id) {
        QuoteBillingMonth entity = quoteBillingMonthRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuoteBillingMonth not found"));
        return QuoteBillingMonthAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthResponseDTO create(QuoteBillingMonthRequestDTO dto) {
        QuoteBillingMonth entity = QuoteBillingMonthAdapter.toEntity(dto);
        entity = quoteBillingMonthRepository.save(entity);
        return QuoteBillingMonthAdapter.toResponseDTO(entity);
    }

    @Override
    public QuoteBillingMonthResponseDTO update(Long id, QuoteBillingMonthRequestDTO dto) {
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
}
