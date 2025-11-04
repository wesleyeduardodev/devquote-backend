package br.com.devquote.enums;
import lombok.Getter;

@Getter
public enum FlowType {

    DESENVOLVIMENTO("Desenvolvimento"),
    OPERACIONAL("Operacional");

    private final String displayName;

    FlowType(String displayName) {
        this.displayName = displayName;
    }

    public static FlowType fromString(String flowType) {
        if (flowType == null) return DESENVOLVIMENTO;

        try {
            return FlowType.valueOf(flowType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DESENVOLVIMENTO;
        }
    }
}
