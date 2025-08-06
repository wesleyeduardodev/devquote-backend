package br.com.devquote.service;
import br.com.devquote.dto.request.MeasurementQuoteRequestDTO;
import br.com.devquote.dto.response.MeasurementQuoteResponseDTO;
import java.util.List;

public interface MeasurementQuoteService {
    List<MeasurementQuoteResponseDTO> findAll();
    MeasurementQuoteResponseDTO findById(Long id);
    MeasurementQuoteResponseDTO create(MeasurementQuoteRequestDTO dto);
    MeasurementQuoteResponseDTO update(Long id, MeasurementQuoteRequestDTO dto);
    void delete(Long id);
}