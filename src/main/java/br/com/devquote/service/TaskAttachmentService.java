package br.com.devquote.service;
import br.com.devquote.dto.response.TaskAttachmentResponse;
import br.com.devquote.entity.TaskAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface TaskAttachmentService {

    List<TaskAttachmentResponse> uploadFiles(Long taskId, List<MultipartFile> files);

    TaskAttachmentResponse uploadFile(Long taskId, MultipartFile file);

    List<TaskAttachmentResponse> getTaskAttachments(Long taskId);

    List<TaskAttachment> getTaskAttachmentsEntities(Long taskId);

    TaskAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAttachments(List<Long> attachmentIds);

    void deleteAllTaskAttachments(Long taskId);

    void deleteAllTaskAttachmentsAndFolder(Long taskId);
}