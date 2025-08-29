package br.com.devquote.service.impl;
import br.com.devquote.adapter.DeliveryAdapter;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.Project;
import br.com.devquote.entity.Quote;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.repository.QuoteRepository;
import br.com.devquote.service.DeliveryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final QuoteRepository quoteRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<DeliveryResponse> findAll() {
        return deliveryRepository.findAllOrderedById().stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryResponse findById(Long id) {
        Delivery entity = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (entity.getQuote() != null) {
            entity.getQuote().getId();
            if (entity.getQuote().getTask() != null) {
                entity.getQuote().getTask().getId();
            }
        }
        if (entity.getProject() != null) {
            entity.getProject().getId();
        }

        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse create(DeliveryRequest dto) {
        Quote quote = quoteRepository.findById(dto.getQuoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Delivery entity = DeliveryAdapter.toEntity(dto, quote, project);
        entity = deliveryRepository.save(entity);
        return DeliveryAdapter.toResponseDTO(entity);
    }

    @Override
    public DeliveryResponse update(Long id, DeliveryRequest dto) {
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

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        deliveryRepository.deleteAllById(ids);
    }

    @Override
    public Page<DeliveryResponse> findAllPaginated(Long id,
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
                                                   Pageable pageable) {

        Page<Delivery> page = deliveryRepository.findByOptionalFieldsPaginated(
                id, taskName, taskCode, projectName,
                branch, pullRequest, status, startedAt, finishedAt, createdAt, updatedAt, pageable
        );

        return page.map(DeliveryAdapter::toResponseDTO);
    }

    @Override
    public Page<DeliveryGroupResponse> findAllGroupedByTask(String taskName,
                                                             String taskCode,
                                                             String status,
                                                             String createdAt,
                                                             String updatedAt,
                                                             Pageable pageable) {
        
        // Buscar todas as entregas com os filtros
        List<Delivery> allDeliveries = deliveryRepository.findByTaskFilters(
                taskName, taskCode, status, createdAt, updatedAt
        );
        
        // Agrupar por Quote ID (que representa uma tarefa)
        Map<Long, List<Delivery>> groupedByQuote = allDeliveries.stream()
                .collect(Collectors.groupingBy(delivery -> delivery.getQuote().getId()));
        
        // Converter para DeliveryGroupResponse
        List<DeliveryGroupResponse> groupedDeliveries = groupedByQuote.entrySet().stream()
                .map(entry -> {
                    Long quoteId = entry.getKey();
                    List<Delivery> deliveries = entry.getValue();
                    Delivery firstDelivery = deliveries.get(0);
                    Quote quote = firstDelivery.getQuote();
                    
                    List<DeliveryResponse> deliveryResponses = deliveries.stream()
                            .map(DeliveryAdapter::toResponseDTO)
                            .collect(Collectors.toList());
                    
                    long completedCount = deliveries.stream()
                            .filter(d -> "COMPLETED".equals(d.getStatus()))
                            .count();
                    
                    long pendingCount = deliveries.stream()
                            .filter(d -> !"COMPLETED".equals(d.getStatus()))
                            .count();
                    
                    return DeliveryGroupResponse.builder()
                            .quoteId(quoteId)
                            .taskName(quote.getTask() != null ? quote.getTask().getTitle() : "N/A")
                            .taskCode(quote.getTask() != null ? quote.getTask().getCode() : "N/A")
                            .quoteStatus(quote.getStatus())
                            .quoteValue(quote.getTotalAmount())
                            .createdAt(quote.getCreatedAt())
                            .updatedAt(quote.getUpdatedAt())
                            .totalDeliveries(deliveries.size())
                            .completedDeliveries((int) completedCount)
                            .pendingDeliveries((int) pendingCount)
                            .deliveries(deliveryResponses)
                            .build();
                })
                .collect(Collectors.toList());
        
        // Aplicar paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), groupedDeliveries.size());
        
        List<DeliveryGroupResponse> pageContent = start >= groupedDeliveries.size() ? 
                List.of() : groupedDeliveries.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, groupedDeliveries.size());
    }

    @Override
    public DeliveryGroupResponse findGroupDetailsByQuoteId(Long quoteId) {
        List<Delivery> deliveries = deliveryRepository.findByQuoteId(quoteId);
        
        if (deliveries.isEmpty()) {
            throw new RuntimeException("No deliveries found for quote ID: " + quoteId);
        }
        
        Delivery firstDelivery = deliveries.get(0);
        Quote quote = firstDelivery.getQuote();
        
        List<DeliveryResponse> deliveryResponses = deliveries.stream()
                .map(DeliveryAdapter::toResponseDTO)
                .collect(Collectors.toList());
        
        long completedCount = deliveries.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .count();
        
        long pendingCount = deliveries.stream()
                .filter(d -> !"COMPLETED".equals(d.getStatus()))
                .count();
        
        return DeliveryGroupResponse.builder()
                .quoteId(quoteId)
                .taskName(quote.getTask() != null ? quote.getTask().getTitle() : "N/A")
                .taskCode(quote.getTask() != null ? quote.getTask().getCode() : "N/A")
                .quoteStatus(quote.getStatus())
                .quoteValue(quote.getTotalAmount())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .totalDeliveries(deliveries.size())
                .completedDeliveries((int) completedCount)
                .pendingDeliveries((int) pendingCount)
                .deliveries(deliveryResponses)
                .build();
    }
}
