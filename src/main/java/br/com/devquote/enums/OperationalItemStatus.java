package br.com.devquote.enums;

import lombok.Getter;

@Getter
public enum OperationalItemStatus {
    PENDING("Pendente"),
    DELIVERED("Entregue");

    private final String descricao;

    OperationalItemStatus(String descricao) {
        this.descricao = descricao;
    }

    public static OperationalItemStatus fromString(String value) {
        if (value == null) {
            return PENDING;
        }
        try {
            return OperationalItemStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
