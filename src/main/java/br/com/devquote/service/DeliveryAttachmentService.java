package br.com.devquote.service;
import br.com.devquote.dto.response.DeliveryAttachmentResponse;
import br.com.devquote.entity.DeliveryAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DeliveryAttachmentService {

    List<DeliveryAttachmentResponse> uploadFiles(Long deliveryId, List<MultipartFile> files);

    DeliveryAttachmentResponse uploadFile(Long deliveryId, MultipartFile file);

    List<DeliveryAttachmentResponse> getDeliveryAttachments(Long deliveryId);

    DeliveryAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAttachments(List<Long> attachmentIds);

    void deleteAllDeliveryAttachments(Long deliveryId);

    void deleteAllDeliveryAttachmentsAndFolder(Long deliveryId);

    List<DeliveryAttachment> getDeliveryAttachmentsEntities(Long deliveryId);
}