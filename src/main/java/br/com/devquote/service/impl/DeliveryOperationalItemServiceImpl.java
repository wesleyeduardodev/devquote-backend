package br.com.devquote.service.impl;

import br.com.devquote.dto.request.DeliveryOperationalItemRequest;
import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import br.com.devquote.dto.response.DeliveryOperationalItemResponse;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryOperationalItem;
import br.com.devquote.enums.OperationalItemStatus;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.DeliveryOperationalItemRepository;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.DeliveryOperationalItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryOperationalItemServiceImpl implements DeliveryOperationalItemService {

    private final DeliveryOperationalItemRepository operationalItemRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryOperationalItemResponse create(DeliveryOperationalItemRequest request) {
        log.debug("Creating operational item for delivery: {}", request.getDeliveryId());

        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        DeliveryOperationalItem item = DeliveryOperationalItem.builder()
                .delivery(delivery)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(OperationalItemStatus.fromString(request.getStatus()))
                .startedAt(request.getStartedAt())
                .finishedAt(request.getFinishedAt())
                .build();

        DeliveryOperationalItem saved = operationalItemRepository.save(item);

        // Atualizar status da delivery
        delivery.updateStatus();
        deliveryRepository.save(delivery);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryOperationalItemResponse update(Long id, DeliveryOperationalItemRequest request) {
        log.debug("Updating operational item: {}", id);

        DeliveryOperationalItem item = operationalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational item not found"));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setStatus(OperationalItemStatus.fromString(request.getStatus()));
        item.setStartedAt(request.getStartedAt());
        item.setFinishedAt(request.getFinishedAt());

        DeliveryOperationalItem updated = operationalItemRepository.save(item);

        // Atualizar status da delivery
        item.getDelivery().updateStatus();
        deliveryRepository.save(item.getDelivery());

        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOperationalItemResponse findById(Long id) {
        DeliveryOperationalItem item = operationalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational item not found"));
        return toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOperationalItemResponse> findByDeliveryId(Long deliveryId) {
        List<DeliveryOperationalItem> items = operationalItemRepository.findByDeliveryIdWithAttachments(deliveryId);
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DeliveryOperationalItem item = operationalItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operational item not found"));

        Delivery delivery = item.getDelivery();
        operationalItemRepository.delete(item);

        // Atualizar status da delivery
        delivery.updateStatus();
        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void deleteByDeliveryId(Long deliveryId) {
        operationalItemRepository.deleteByDeliveryId(deliveryId);
    }

    private DeliveryOperationalItemResponse toResponse(DeliveryOperationalItem item) {
        List<DeliveryOperationalAttachmentResponse> attachmentResponses = item.getAttachments() != null
                ? item.getAttachments().stream()
                        .map(att -> DeliveryOperationalAttachmentResponse.builder()
                                .id(att.getId())
                                .deliveryOperationalItemId(item.getId())
                                .fileName(att.getFileName())
                                .originalName(att.getOriginalName())
                                .filePath(att.getFilePath())
                                .fileSize(att.getFileSize())
                                .contentType(att.getContentType())
                                .uploadedAt(att.getUploadedAt())
                                .createdAt(att.getCreatedAt())
                                .updatedAt(att.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        return DeliveryOperationalItemResponse.builder()
                .id(item.getId())
                .deliveryId(item.getDelivery().getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .status(item.getStatus().name())
                .startedAt(item.getStartedAt())
                .finishedAt(item.getFinishedAt())
                .attachments(attachmentResponses)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
