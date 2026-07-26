package br.com.devquote.service;
import br.com.devquote.dto.response.BillingNoteAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface BillingNoteAttachmentService {

    List<BillingNoteAttachmentResponse> uploadFiles(Long billingNoteId, List<MultipartFile> files);

    BillingNoteAttachmentResponse uploadFile(Long billingNoteId, MultipartFile file);

    List<BillingNoteAttachmentResponse> getBillingNoteAttachments(Long billingNoteId);

    BillingNoteAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAllByBillingNote(Long billingNoteId);

    Map<Long, Integer> countByBillingNoteIds(List<Long> billingNoteIds);
}
