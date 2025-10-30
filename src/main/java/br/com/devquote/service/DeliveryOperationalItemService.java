package br.com.devquote.service;

import br.com.devquote.dto.request.DeliveryOperationalItemRequest;
import br.com.devquote.dto.response.DeliveryOperationalItemResponse;

import java.util.List;

public interface DeliveryOperationalItemService {

    DeliveryOperationalItemResponse create(DeliveryOperationalItemRequest request);

    DeliveryOperationalItemResponse update(Long id, DeliveryOperationalItemRequest request);

    DeliveryOperationalItemResponse findById(Long id);

    List<DeliveryOperationalItemResponse> findByDeliveryId(Long deliveryId);

    void delete(Long id);

    void deleteByDeliveryId(Long deliveryId);
}
