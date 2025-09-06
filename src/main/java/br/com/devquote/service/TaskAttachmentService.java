package br.com.devquote.service;

import br.com.devquote.dto.response.TaskAttachmentResponse;
import br.com.devquote.entity.TaskAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskAttachmentService {

    /**
     * Faz upload de arquivos para uma tarefa
     */
    List<TaskAttachmentResponse> uploadFiles(Long taskId, List<MultipartFile> files);

    /**
     * Faz upload de um arquivo para uma tarefa
     */
    TaskAttachmentResponse uploadFile(Long taskId, MultipartFile file);

    /**
     * Lista todos os anexos de uma tarefa
     */
    List<TaskAttachmentResponse> getTaskAttachments(Long taskId);

    /**
     * Busca anexo por ID
     */
    TaskAttachmentResponse getAttachmentById(Long attachmentId);

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
     * Exclui todos os anexos de uma tarefa (soft delete)
     */
    void deleteAllTaskAttachments(Long taskId);
}