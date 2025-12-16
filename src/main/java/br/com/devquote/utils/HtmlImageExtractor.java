package br.com.devquote.utils;

import br.com.devquote.dto.response.ContentBlock;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HtmlImageExtractor {

    private static final Pattern IMG_TAG_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final boolean RESIZE_IMAGES = false;

    private static final String INLINE_IMAGE_PREFIX = "/api/inline-images/view/";

    public static List<ContentBlock> parseHtmlToBlocks(String html) {
        return parseHtmlToBlocks(html, null);
    }

    public static List<ContentBlock> parseHtmlToBlocks(String html, FileStorageStrategy fileStorageStrategy) {
        List<ContentBlock> blocks = new ArrayList<>();

        if (html == null || html.isEmpty()) {
            return blocks;
        }

        Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        int lastEnd = 0;
        int order = 1;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textBefore = html.substring(lastEnd, matcher.start());
                String cleanText = stripHtmlTags(textBefore);
                if (!cleanText.isBlank()) {
                    blocks.add(ContentBlock.textBlock(cleanText.trim(), order++));
                }
            }

            String imageUrl = decodeHtmlEntities(matcher.group(1));
            byte[] imageBytes = downloadImage(imageUrl, fileStorageStrategy);
            if (imageBytes != null) {
                blocks.add(ContentBlock.imageBlock(imageBytes, order++));
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < html.length()) {
            String textAfter = html.substring(lastEnd);
            String cleanText = stripHtmlTags(textAfter);
            if (!cleanText.isBlank()) {
                blocks.add(ContentBlock.textBlock(cleanText.trim(), order));
            }
        }

        log.debug("Parsed HTML into {} content blocks", blocks.size());
        return blocks;
    }

    public static List<String> extractImageUrls(String html) {
        List<String> urls = new ArrayList<>();
        if (html == null || html.isEmpty()) {
            return urls;
        }

        Matcher matcher = IMG_TAG_PATTERN.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isEmpty()) {
                urls.add(url);
            }
        }
        return urls;
    }

    public static String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        String noImages = html.replaceAll("<img[^>]*>", "");
        String noTags = noImages.replaceAll("<[^>]+>", " ");
        String decoded = decodeHtmlEntities(noTags);

        return decoded.replaceAll("\\s+", " ").trim();
    }

    public static String decodeHtmlEntities(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text
                .replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&#x2F;", "/");
    }

    public static boolean hasImages(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }
        return IMG_TAG_PATTERN.matcher(html).find();
    }

    public static byte[] downloadImage(String imageUrl) {
        return downloadImage(imageUrl, null);
    }

    public static byte[] downloadImage(String imageUrl, FileStorageStrategy fileStorageStrategy) {
        try {
            if (imageUrl.startsWith(INLINE_IMAGE_PREFIX) && fileStorageStrategy != null) {
                String filePath = imageUrl.substring(INLINE_IMAGE_PREFIX.length());
                log.debug("Loading internal image from storage: {}", filePath);
                InputStream inputStream = fileStorageStrategy.getFileStream(filePath);
                byte[] imageBytes = inputStream.readAllBytes();
                inputStream.close();
                return resizeImageIfNeeded(imageBytes);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                byte[] imageBytes = response.body();
                return resizeImageIfNeeded(imageBytes);
            } else {
                log.warn("Failed to download image from {}: HTTP {}", imageUrl, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error downloading image from {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    public static List<byte[]> downloadImages(List<String> imageUrls) {
        List<byte[]> images = new ArrayList<>();
        for (String url : imageUrls) {
            byte[] imageBytes = downloadImage(url);
            if (imageBytes != null) {
                images.add(imageBytes);
            }
        }
        return images;
    }

    private static byte[] resizeImageIfNeeded(byte[] originalBytes) {
        if (!RESIZE_IMAGES) {
            return originalBytes;
        }
        return originalBytes;
    }

    public static InputStream toInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }
}
