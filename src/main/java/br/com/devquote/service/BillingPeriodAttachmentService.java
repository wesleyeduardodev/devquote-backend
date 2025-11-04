package br.com.devquote.service;
import br.com.devquote.dto.response.BillingPeriodAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BillingPeriodAttachmentService {

    List<BillingPeriodAttachmentResponse> uploadFiles(Long billingPeriodId, List<MultipartFile> files);

    BillingPeriodAttachmentResponse uploadFile(Long billingPeriodId, MultipartFile file);

    List<BillingPeriodAttachmentResponse> getBillingPeriodAttachments(Long billingPeriodId);

    BillingPeriodAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAttachments(List<Long> attachmentIds);

    void deleteAllBillingPeriodAttachments(Long billingPeriodId);

    void deleteAllBillingPeriodAttachmentsAndFolder(Long billingPeriodId);

    List<br.com.devquote.entity.BillingPeriodAttachment> getBillingPeriodAttachmentsEntities(Long billingPeriodId);
}