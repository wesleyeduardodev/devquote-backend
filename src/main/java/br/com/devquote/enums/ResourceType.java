package br.com.devquote.enums;

public enum ResourceType {
    BILLING("billing", "Faturamento", "Gestão de períodos de faturamento"),
    QUOTES("quotes", "Orçamentos", "Gestão de orçamentos e cotações"),
    TASKS("tasks", "Tarefas", "Gestão de tarefas e atividades"),
    PROJECTS("projects", "Projetos", "Gestão de projetos"),
    DELIVERIES("deliveries", "Entregas", "Gestão de entregas e deliveries"),
    USERS("users", "Usuários", "Gestão de usuários do sistema"),
    REPORTS("reports", "Relatórios", "Visualização de relatórios"),
    SETTINGS("settings", "Configurações", "Configurações do sistema");

    private final String code;
    private final String name;
    private final String description;

    ResourceType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}