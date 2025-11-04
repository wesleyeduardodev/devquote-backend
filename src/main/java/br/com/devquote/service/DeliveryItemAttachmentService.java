package br.com.devquote.service;
import br.com.devquote.dto.response.DeliveryItemAttachmentResponse;
import br.com.devquote.entity.DeliveryItemAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DeliveryItemAttachmentService {

    List<DeliveryItemAttachmentResponse> uploadFiles(Long deliveryItemId, List<MultipartFile> files);

    DeliveryItemAttachmentResponse uploadFile(Long deliveryItemId, MultipartFile file);

    List<DeliveryItemAttachmentResponse> getDeliveryItemAttachments(Long deliveryItemId);

    DeliveryItemAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAttachments(List<Long> attachmentIds);

    void deleteAllDeliveryItemAttachments(Long deliveryItemId);

    void deleteAllDeliveryItemAttachmentsByDeliveryId(Long deliveryId);

    List<DeliveryItemAttachment> getDeliveryItemAttachmentsEntitiesByDeliveryId(Long deliveryId);
}