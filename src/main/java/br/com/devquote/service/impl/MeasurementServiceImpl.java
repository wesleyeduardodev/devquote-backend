package br.com.devquote.service.impl;
import br.com.devquote.adapter.MeasurementAdapter;
import br.com.devquote.dto.request.MeasurementRequestDTO;
import br.com.devquote.dto.response.MeasurementResponseDTO;
import br.com.devquote.entity.Measurement;
import br.com.devquote.repository.MeasurementRepository;
import br.com.devquote.service.MeasurementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MeasurementServiceImpl implements MeasurementService {

    private final MeasurementRepository measurementRepository;

    @Override
    public List<MeasurementResponseDTO> findAll() {
        return measurementRepository.findAll().stream()
                .map(MeasurementAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MeasurementResponseDTO findById(Long id) {
        Measurement entity = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        return MeasurementAdapter.toResponseDTO(entity);
    }

    @Override
    public MeasurementResponseDTO create(MeasurementRequestDTO dto) {
        Measurement entity = MeasurementAdapter.toEntity(dto);
        entity = measurementRepository.save(entity);
        return MeasurementAdapter.toResponseDTO(entity);
    }

    @Override
    public MeasurementResponseDTO update(Long id, MeasurementRequestDTO dto) {
        Measurement entity = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        MeasurementAdapter.updateEntityFromDto(dto, entity);
        entity = measurementRepository.save(entity);
        return MeasurementAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        measurementRepository.deleteById(id);
    }
}