package br.com.devquote.service;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DeliveryService {
    List<DeliveryResponseDTO> findAll();

    DeliveryResponseDTO findById(Long id);

    DeliveryResponseDTO create(DeliveryRequestDTO dto);

    DeliveryResponseDTO update(Long id, DeliveryRequestDTO dto);

    void delete(Long id);

    Page<DeliveryResponseDTO> findAllPaginated(Long id,
                                               String taskName,
                                               String taskCode,
                                               String projectName,
                                               String branch,
                                               String pullRequest,
                                               String status,
                                               String startedAt,
                                               String finishedAt,
                                               String createdAt,
                                               String updatedAt,
                                               Pageable pageable);
}