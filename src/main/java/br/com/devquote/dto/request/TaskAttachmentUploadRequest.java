package br.com.devquote.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskAttachmentUploadRequest {

    @NotNull(message = "Task ID é obrigatório")
    private Long taskId;

    // O arquivo será enviado como MultipartFile separadamente
}