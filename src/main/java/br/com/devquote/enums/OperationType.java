package br.com.devquote.enums;

public enum OperationType {
    CREATE("CREATE", "Criar", "Criar novos registros"),
    READ("READ", "Visualizar", "Visualizar registros existentes"),
    UPDATE("UPDATE", "Editar", "Editar registros existentes"),
    DELETE("DELETE", "Excluir", "Excluir registros"),
    BULK("BULK", "Operações em Massa", "Realizar operações em múltiplos registros");

    private final String code;
    private final String name;
    private final String description;

    OperationType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}