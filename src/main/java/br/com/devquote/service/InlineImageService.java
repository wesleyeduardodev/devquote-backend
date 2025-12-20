package br.com.devquote.service;

import br.com.devquote.dto.response.InlineImageResponse;
import br.com.devquote.enums.InlineImageEntityType;
import org.springframework.web.multipart.MultipartFile;

public interface InlineImageService {
    InlineImageResponse uploadImage(MultipartFile file, InlineImageEntityType entityType, Long entityId, Long parentId);
}
