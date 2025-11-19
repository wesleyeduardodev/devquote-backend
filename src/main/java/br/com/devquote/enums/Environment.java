package br.com.devquote.enums;
import lombok.Getter;

@Getter
public enum Environment {

    DESENVOLVIMENTO("Desenvolvimento"),
    HOMOLOGACAO("Homologação"),
    PRODUCAO("Produção");

    private final String displayName;

    Environment(String displayName) {
        this.displayName = displayName;
    }

    public static Environment fromString(String environment) {
        if (environment == null) return null;

        try {
            return Environment.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
