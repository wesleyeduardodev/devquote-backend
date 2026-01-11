package br.com.devquote.enums;

import br.com.devquote.enums.DeliveryStatus;

public enum ClickUpStatusMapping {

    DEVELOPMENT("em progresso"),
    DELIVERED("desenvolvimento concluído"),
    PRODUCTION("complete");

    private final String clickUpStatus;

    ClickUpStatusMapping(String clickUpStatus) {
        this.clickUpStatus = clickUpStatus;
    }

    public String getClickUpStatus() {
        return clickUpStatus;
    }

    public static String fromDeliveryStatus(DeliveryStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case DEVELOPMENT -> DEVELOPMENT.getClickUpStatus();
            case DELIVERED -> DELIVERED.getClickUpStatus();
            case PRODUCTION -> PRODUCTION.getClickUpStatus();
            default -> null;
        };
    }

    public static boolean isSyncableStatus(DeliveryStatus status) {
        return status == DeliveryStatus.DEVELOPMENT
                || status == DeliveryStatus.DELIVERED
                || status == DeliveryStatus.PRODUCTION;
    }
}
