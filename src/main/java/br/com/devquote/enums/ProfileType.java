package br.com.devquote.enums;

import lombok.Getter;

@Getter
public enum ProfileType {

    ADMIN("ADMIN", "Administrador", "Acesso total ao sistema", 1),
    MANAGER("MANAGER", "Gerente", "Gestão operacional e supervisão", 2),
    USER("USER", "Usuário", "Operações básicas limitadas", 3);

    private final String code;
    private final String name;
    private final String description;
    private final int level;

    ProfileType(String code, String name, String description, int level) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.level = level;
    }
}
