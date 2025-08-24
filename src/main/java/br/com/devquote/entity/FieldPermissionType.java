package br.com.devquote.entity;

public enum FieldPermissionType {
    READ("Somente Leitura"),
    EDIT("Leitura e Edição"),
    HIDDEN("Campo Oculto");

    private final String description;

    FieldPermissionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}