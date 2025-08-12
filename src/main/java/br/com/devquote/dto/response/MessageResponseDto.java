package br.com.devquote.dto.response;
import lombok.Data;

@Data
public class MessageResponseDto {

    private String message;

    public MessageResponseDto(String message) {
        this.message = message;
    }
}