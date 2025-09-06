package br.com.devquote.service;

import br.com.devquote.dto.response.DeliveryAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeliveryAttachmentService {

    /**
     * Faz upload de arquivos para uma entrega
     */
    List<DeliveryAttachmentResponse> uploadFiles(Long deliveryId, List<MultipartFile> files);

    /**
     * Faz upload de um arquivo para uma entrega
     */
    DeliveryAttachmentResponse uploadFile(Long deliveryId, MultipartFile file);

    /**
     * Lista todos os anexos de uma entrega
     */
    List<DeliveryAttachmentResponse> getDeliveryAttachments(Long deliveryId);

    /**
     * Busca anexo por ID
     */
    DeliveryAttachmentResponse getAttachmentById(Long attachmentId);

    /**
     * Faz download de um anexo
     */
    Resource downloadAttachment(Long attachmentId);

    /**
     * Exclui um anexo
     */
    void deleteAttachment(Long attachmentId);

    /**
     * Exclui múltiplos anexos
     */
    void deleteAttachments(List<Long> attachmentIds);

    /**
     * Exclui todos os anexos de uma entrega
     */
    void deleteAllDeliveryAttachments(Long deliveryId);

    /**
     * Exclui todos os anexos de uma entrega e remove completamente a pasta do storage
     */
    void deleteAllDeliveryAttachmentsAndFolder(Long deliveryId);
}