package br.com.devquote.service;

import br.com.devquote.dto.response.DeliveryOperationalAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DeliveryOperationalAttachmentService {

    List<DeliveryOperationalAttachmentResponse> uploadFiles(Long operationalItemId, List<MultipartFile> files) throws IOException;

    DeliveryOperationalAttachmentResponse findById(Long id);

    List<DeliveryOperationalAttachmentResponse> findByOperationalItemId(Long operationalItemId);

    void delete(Long id) throws IOException;

    Resource downloadFile(Long id) throws IOException;

    void deleteAllOperationalAttachmentsByDeliveryId(Long deliveryId) throws IOException;

    /**
     * Busca anexos operacionais como entidades por delivery ID (para uso interno com emails)
     */
    List<br.com.devquote.entity.DeliveryOperationalAttachment> getOperationalAttachmentsEntitiesByDeliveryId(Long deliveryId);
}
