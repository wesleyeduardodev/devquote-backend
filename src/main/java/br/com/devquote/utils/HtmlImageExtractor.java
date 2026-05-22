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

    private static final Pattern PRE_BLOCK_PATTERN = Pattern.compile(
            "<pre[^>]*>(.*?)</pre>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
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
                String cleanText = cleanHtmlForPdf(textBefore);
                if (cleanText != null && !cleanText.isBlank()) {
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
            String cleanText = cleanHtmlForPdf(textAfter);
            if (cleanText != null && !cleanText.isBlank()) {
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

        String result = html.replaceAll("<img[^>]*>", "[imagem]");

        result = result.replaceAll("</p>", "\n");
        result = result.replaceAll("</div>", "\n");
        result = result.replaceAll("<br\\s*/?>", "\n");

        result = result.replaceAll("<[^>]+>", "");

        result = decodeHtmlEntities(result);

        result = result.replaceAll("[ \\t]+", " ");

        result = result.replaceAll(" *\\n *", "\n");
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    public static String cleanHtmlForPdf(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // Protege blocos de código (<pre>) antes das limpezas que colapsam espaços/quebras,
        // pois SQL e afins dependem de indentação e quebras de linha.
        List<String> codeBlocks = new ArrayList<>();
        Matcher preMatcher = PRE_BLOCK_PATTERN.matcher(html);
        StringBuffer protectedBuffer = new StringBuffer();
        while (preMatcher.find()) {
            codeBlocks.add(formatCodeBlock(preMatcher.group(1)));
            String token = "@@DQCODE" + (codeBlocks.size() - 1) + "@@";
            preMatcher.appendReplacement(protectedBuffer, Matcher.quoteReplacement(token));
        }
        preMatcher.appendTail(protectedBuffer);

        String result = protectedBuffer.toString().replaceAll("<img[^>]*>", "[imagem]");

        result = result.replaceAll("</p>", "<br/>");
        result = result.replaceAll("</div>", "<br/>");
        result = result.replaceAll("<br\\s*/?>", "<br/>");

        result = convertStylesToFontTags(result);

        result = result.replaceAll("<(p|div)[^>]*>", "");

        result = result.replaceAll("<strong>", "<b>");
        result = result.replaceAll("</strong>", "</b>");
        result = result.replaceAll("<em>", "<i>");
        result = result.replaceAll("</em>", "</i>");

        result = decodeHtmlEntities(result);

        result = result.replaceAll("[ \\t]+", " ");
        result = result.replaceAll("(<br/>\\s*){3,}", "<br/><br/>");
        result = result.replaceAll("^\\s*(<br/>\\s*)+", "");
        result = result.replaceAll("(<br/>\\s*)+$", "");

        result = result.trim();

        // Reinsere os blocos de código já formatados (monoespaçado, com indentação e quebras preservadas).
        for (int i = 0; i < codeBlocks.size(); i++) {
            result = result.replace("@@DQCODE" + i + "@@", codeBlocks.get(i));
        }

        return result;
    }

    private static String formatCodeBlock(String codeHtml) {
        if (codeHtml == null || codeHtml.isEmpty()) {
            return "";
        }

        String code = codeHtml.replaceAll("(?i)</?code[^>]*>", "");
        code = code.replace("\r\n", "\n").replace("\r", "\n");
        code = code.replaceAll("(?i)<br\\s*/?>", "\n");
        code = code.replace("\t", "    ");
        code = code.replaceAll("^\\n+", "").replaceAll("\\n+$", "");

        // Preserva indentação (espaços -> &nbsp;) e quebras de linha (\n -> <br/>).
        // Mantém entidades já existentes (&lt;, &gt;, &amp;...) para o markup HTML renderizar literalmente.
        code = code.replace(" ", "&nbsp;");
        code = code.replace("\n", "<br/>");

        return "<font face=\"DejaVu Sans Mono\">" + code + "</font>";
    }

    private static String convertStylesToFontTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        String result = html;

        Pattern colorPattern = Pattern.compile(
            "<span[^>]*style=\"[^\"]*color:\\s*([^;\"]+)[^\"]*\"[^>]*>",
            Pattern.CASE_INSENSITIVE
        );
        Matcher colorMatcher = colorPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (colorMatcher.find()) {
            String color = colorMatcher.group(1).trim();
            colorMatcher.appendReplacement(sb, "<font color=\"" + color + "\">");
        }
        colorMatcher.appendTail(sb);
        result = sb.toString();

        result = result.replaceAll("</span>", "</font>");

        result = result.replaceAll("<span[^>]*>", "");

        result = result.replaceAll("<mark[^>]*>", "");
        result = result.replaceAll("</mark>", "");

        return result;
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
