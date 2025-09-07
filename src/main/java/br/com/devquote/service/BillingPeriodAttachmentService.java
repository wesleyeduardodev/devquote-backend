package br.com.devquote.service;

import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BillingPeriodAttachmentService {

    /**
     * Faz upload de arquivos para um período de faturamento
     */
    List<BillingPeriodAttachmentResponse> uploadFiles(Long billingPeriodId, List<MultipartFile> files);

    /**
     * Faz upload de um arquivo para um período de faturamento
     */
    BillingPeriodAttachmentResponse uploadFile(Long billingPeriodId, MultipartFile file);

    /**
     * Lista todos os anexos de um período de faturamento
     */
    List<BillingPeriodAttachmentResponse> getBillingPeriodAttachments(Long billingPeriodId);

    /**
     * Busca anexo por ID
     */
    BillingPeriodAttachmentResponse getAttachmentById(Long attachmentId);

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
     * Exclui todos os anexos de um período de faturamento (soft delete)
     */
    void deleteAllBillingPeriodAttachments(Long billingPeriodId);

    /**
     * Exclui todos os anexos de um período de faturamento e remove completamente a pasta do storage
     */
    void deleteAllBillingPeriodAttachmentsAndFolder(Long billingPeriodId);
    
    /**
     * Busca anexos como entidades (para uso interno com emails)
     */
    List<br.com.devquote.entity.BillingPeriodAttachment> getBillingPeriodAttachmentsEntities(Long billingPeriodId);
}