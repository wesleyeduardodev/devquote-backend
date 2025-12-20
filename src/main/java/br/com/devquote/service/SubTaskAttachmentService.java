package br.com.devquote.service;

import br.com.devquote.dto.response.SubTaskAttachmentResponse;
import br.com.devquote.entity.SubTaskAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface SubTaskAttachmentService {

    List<SubTaskAttachmentResponse> uploadFiles(Long subTaskId, List<MultipartFile> files);

    SubTaskAttachmentResponse uploadFile(Long subTaskId, MultipartFile file);

    List<SubTaskAttachmentResponse> getSubTaskAttachments(Long subTaskId);

    List<SubTaskAttachment> getSubTaskAttachmentsEntities(Long subTaskId);

    SubTaskAttachmentResponse getAttachmentById(Long attachmentId);

    Resource downloadAttachment(Long attachmentId);

    void deleteAttachment(Long attachmentId);

    void deleteAttachments(List<Long> attachmentIds);

    void deleteAllSubTaskAttachments(Long subTaskId);

    void deleteAllSubTaskAttachmentsAndFolder(Long subTaskId);
}
