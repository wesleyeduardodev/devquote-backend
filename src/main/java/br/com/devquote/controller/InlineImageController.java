package br.com.devquote.controller;

import br.com.devquote.dto.response.InlineImageResponse;
import br.com.devquote.service.InlineImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inline-images")
@RequiredArgsConstructor
@Slf4j
public class InlineImageController {

    private final InlineImageService inlineImageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<InlineImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "context", defaultValue = "general") String context) {

        try {
            InlineImageResponse response = inlineImageService.uploadImage(file, context);
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
}
