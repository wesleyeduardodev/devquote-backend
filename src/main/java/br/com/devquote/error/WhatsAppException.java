package br.com.devquote.error;

public class WhatsAppException extends RuntimeException {

    private final String code;

    public WhatsAppException(String message) {
        super(message);
        this.code = "WHATSAPP_ERROR";
    }

    public WhatsAppException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
