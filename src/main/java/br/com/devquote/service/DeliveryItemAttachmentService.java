package br.com.devquote.service;

import br.com.devquote.dto.response.DeliveryItemAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeliveryItemAttachmentService {

    /**
     * Faz upload de arquivos para um item de entrega
     */
    List<DeliveryItemAttachmentResponse> uploadFiles(Long deliveryItemId, List<MultipartFile> files);

    /**
     * Faz upload de um arquivo para um item de entrega
     */
    DeliveryItemAttachmentResponse uploadFile(Long deliveryItemId, MultipartFile file);

    /**
     * Lista todos os anexos de um item de entrega
     */
    List<DeliveryItemAttachmentResponse> getDeliveryItemAttachments(Long deliveryItemId);

    /**
     * Busca anexo por ID
     */
    DeliveryItemAttachmentResponse getAttachmentById(Long attachmentId);

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
     * Exclui todos os anexos de um item de entrega
     */
    void deleteAllDeliveryItemAttachments(Long deliveryItemId);

    /**
     * Exclui todos os anexos de todos os itens de uma entrega
     */
    void deleteAllDeliveryItemAttachmentsByDeliveryId(Long deliveryId);

    /**
     * Exclui todos os anexos de um item de entrega e remove completamente a pasta do storage
     */
    void deleteAllDeliveryItemAttachmentsAndFolder(Long deliveryItemId);
}