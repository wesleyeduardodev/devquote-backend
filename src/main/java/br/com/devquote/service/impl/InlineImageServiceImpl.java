package br.com.devquote.service.impl;

import br.com.devquote.dto.response.InlineImageResponse;
import br.com.devquote.service.InlineImageService;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InlineImageServiceImpl implements InlineImageService {

    private final FileStorageStrategy fileStorageStrategy;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_WIDTH = 1200;
    private static final float COMPRESSION_QUALITY = 0.85f;

    @Override
    public InlineImageResponse uploadImage(MultipartFile file, String context) {
        validateImage(file);

        try {
            MultipartFile processedFile = compressImageIfNeeded(file);

            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = buildFilePath(context, fileName);

            String uploadedFilePath = fileStorageStrategy.uploadFile(processedFile, filePath);

            String proxyUrl = "/api/inline-images/view/" + uploadedFilePath;

            log.info("Inline image uploaded successfully: {} (original size: {}, compressed size: {})",
                    fileName, file.getSize(), processedFile.getSize());

            return InlineImageResponse.builder()
                    .url(proxyUrl)
                    .fileName(fileName)
                    .contentType(processedFile.getContentType())
                    .fileSize(processedFile.getSize())
                    .build();

        } catch (IOException e) {
            log.error("Error uploading inline image: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao fazer upload da imagem: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Arquivo de imagem nao pode estar vazio");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Imagem muito grande. Tamanho maximo: 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("Tipo de imagem nao permitido. Use JPEG, PNG, GIF ou WebP");
        }
    }

    private MultipartFile compressImageIfNeeded(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if ("image/gif".equals(contentType)) {
            return file;
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            return file;
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= MAX_WIDTH && file.getSize() <= 500 * 1024) {
            return file;
        }

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > MAX_WIDTH) {
            newWidth = MAX_WIDTH;
            newHeight = (int) ((double) originalHeight * MAX_WIDTH / originalWidth);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight,
                contentType.equals("image/png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String formatName = getFormatName(contentType);
        ImageIO.write(resizedImage, formatName, outputStream);

        byte[] compressedBytes = outputStream.toByteArray();

        return new CompressedMultipartFile(
                file.getName(),
                file.getOriginalFilename(),
                contentType,
                compressedBytes
        );
    }

    private String getFormatName(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "img_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String buildFilePath(String context, String fileName) {
        String sanitizedContext = context.replaceAll("[^a-zA-Z0-9_-]", "_");
        return String.format("inline-images/%s/%s", sanitizedContext, fileName);
    }

    private static class CompressedMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public CompressedMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
