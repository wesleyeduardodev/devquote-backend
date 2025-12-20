package br.com.devquote.controller;

import br.com.devquote.dto.response.InlineImageResponse;
import br.com.devquote.enums.InlineImageEntityType;
import br.com.devquote.service.InlineImageService;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/inline-images")
@RequiredArgsConstructor
@Slf4j
public class InlineImageController {

    private final InlineImageService inlineImageService;
    private final FileStorageStrategy fileStorageStrategy;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<InlineImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") InlineImageEntityType entityType,
            @RequestParam("entityId") Long entityId,
            @RequestParam(value = "parentId", required = false) Long parentId) {

        try {
            InlineImageResponse response = inlineImageService.uploadImage(file, entityType, entityId, parentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error uploading inline image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(InlineImageResponse.builder()
                            .url(null)
                            .fileName(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error uploading inline image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view/**")
    public ResponseEntity<byte[]> viewImage(HttpServletRequest request) {
        try {
            String fullPath = request.getRequestURI();
            String filePath = fullPath.substring(fullPath.indexOf("/view/") + 6);

            if (filePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            InputStream inputStream = fileStorageStrategy.getFileStream(filePath);
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            MediaType mediaType = determineMediaType(filePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("Error serving inline image: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType determineMediaType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerPath.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerPath.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
