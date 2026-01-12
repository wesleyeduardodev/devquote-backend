package br.com.devquote.enums;

import java.util.HashMap;
import java.util.Map;

public enum ClickUpStatusOrder {

    BACKLOG("backlog", 1),
    EM_ANALISE_SUPORTE("em análise - suporte", 2),
    A_INICIAR_DEV_INTERNO("a iniciar - dev interno", 3),
    EM_PROGRESSO("em progresso", 4),
    DESENVOLVIMENTO_CONCLUIDO("desenvolvimento concluído", 5),
    PRONTO_PARA_TESTES("pronto para testes", 6),
    TESTES_CONCLUIDOS("testes concluídos", 7),
    COMPLETE("complete", 100),
    CONCLUIDO("concluído", 100);

    private final String clickUpStatus;
    private final int order;

    private static final Map<String, ClickUpStatusOrder> STATUS_MAP = new HashMap<>();

    static {
        for (ClickUpStatusOrder status : values()) {
            STATUS_MAP.put(normalizeStatus(status.clickUpStatus), status);
        }
    }

    ClickUpStatusOrder(String clickUpStatus, int order) {
        this.clickUpStatus = clickUpStatus;
        this.order = order;
    }

    public String getClickUpStatus() {
        return clickUpStatus;
    }

    public int getOrder() {
        return order;
    }

    public static int getOrderByStatus(String status) {
        if (status == null) {
            return 0;
        }
        ClickUpStatusOrder found = STATUS_MAP.get(normalizeStatus(status));
        return found != null ? found.getOrder() : 0;
    }

    public static boolean canAdvanceTo(String currentStatus, String newStatus) {
        int currentOrder = getOrderByStatus(currentStatus);
        int newOrder = getOrderByStatus(newStatus);
        return newOrder > currentOrder;
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        return status.toLowerCase().trim();
    }
}
