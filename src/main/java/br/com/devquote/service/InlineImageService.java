package br.com.devquote.service;

import br.com.devquote.dto.response.InlineImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface InlineImageService {
    InlineImageResponse uploadImage(MultipartFile file, String context);
}
