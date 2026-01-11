package br.com.devquote.error;

public class GitProviderException extends RuntimeException {

    private final String code;

    public GitProviderException(String message) {
        super(message);
        this.code = "GIT_PROVIDER_ERROR";
    }

    public GitProviderException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
