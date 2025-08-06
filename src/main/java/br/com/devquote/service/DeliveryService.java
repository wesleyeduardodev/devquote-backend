package br.com.devquote.service;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import java.util.List;

public interface DeliveryService {
    List<DeliveryResponseDTO> findAll();
    DeliveryResponseDTO findById(Long id);
    DeliveryResponseDTO create(DeliveryRequestDTO dto);
    DeliveryResponseDTO update(Long id, DeliveryRequestDTO dto);
    void delete(Long id);
}