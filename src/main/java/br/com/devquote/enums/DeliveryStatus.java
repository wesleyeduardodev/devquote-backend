package br.com.devquote.enums;
import lombok.Getter;

@Getter
public enum DeliveryStatus {

    PENDING("Pendente"),
    DEVELOPMENT("Desenvolvimento"),
    DELIVERED("Entregue"),
    HOMOLOGATION("Homologação"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    PRODUCTION("Produção"),
    CANCELLED("Cancelado");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public static DeliveryStatus fromString(String status) {
        if (status == null) return PENDING;
        
        try {
            return DeliveryStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}