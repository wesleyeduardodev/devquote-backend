package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InlineImageResponse {
    private String url;
    private String fileName;
    private String contentType;
    private Long fileSize;
}
