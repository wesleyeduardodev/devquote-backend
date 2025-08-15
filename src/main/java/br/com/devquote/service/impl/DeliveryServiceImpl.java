package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAdapter;
import br.com.devquote.dto.request.DeliveryRequestDTO;
import br.com.devquote.dto.response.DeliveryResponseDTO;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Quote;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.service.DeliveryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final QuoteRepository quoteRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<DeliveryResponseDTO> findAll() {
        return deliveryRepository.findAllOrderedById().stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryResponseDTO findById(Long id) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponseDTO create(DeliveryRequestDTO dto) {
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Delivery entity = DeliveryAdapter.toEntity(dto, quote, project);
        entity = deliveryRepository.save(entity);
        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponseDTO update(Long id, DeliveryRequestDTO dto) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        DeliveryAdapter.updateEntityFromDto(dto, entity, quote, project);
        entity = deliveryRepository.save(entity);
        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        deliveryRepository.deleteById(id);
    }
}
