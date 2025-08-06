package br.com.devquote.service;
import br.com.devquote.dto.request.MeasurementRequestDTO;
import br.com.devquote.dto.response.MeasurementResponseDTO;
import java.util.List;

public interface MeasurementService {
    List<MeasurementResponseDTO> findAll();
    MeasurementResponseDTO findById(Long id);
    MeasurementResponseDTO create(MeasurementRequestDTO dto);
    MeasurementResponseDTO update(Long id, MeasurementRequestDTO dto);
    void delete(Long id);
}