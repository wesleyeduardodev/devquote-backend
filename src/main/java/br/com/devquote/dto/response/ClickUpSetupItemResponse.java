package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Item genérico {id, name} — usado pra workspaces, spaces e lists no wizard. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickUpSetupItemResponse {
    private String id;
    private String name;
    /** Opcional — em listas: nome da pasta. Em outros tipos: null. */
    private String parent;
}
