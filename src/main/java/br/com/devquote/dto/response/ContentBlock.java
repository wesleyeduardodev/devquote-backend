package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBlock {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";

    private String type;
    private String text;
    private byte[] imageBytes;
    private Integer order;

    public boolean checkIsText() {
        return TYPE_TEXT.equals(type);
    }

    public boolean checkIsImage() {
        return TYPE_IMAGE.equals(type);
    }

    public static ContentBlock textBlock(String text, int order) {
        return ContentBlock.builder()
                .type(TYPE_TEXT)
                .text(text)
                .order(order)
                .build();
    }

    public static ContentBlock imageBlock(byte[] imageBytes, int order) {
        return ContentBlock.builder()
                .type(TYPE_IMAGE)
                .imageBytes(imageBytes)
                .order(order)
                .build();
    }
}
