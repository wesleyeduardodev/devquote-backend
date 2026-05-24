package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta da listagem de custom fields de uma list, com sugestões auto-detectadas
 * pra economizar trabalho do user no wizard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickUpSetupFieldsResponse {

    /** Todos os custom fields da list (pra dropdown manual). */
    private List<Field> all;

    /** ID do field detectado como "Desenvolvedor" (match por nome). Null se não detectou. */
    private String suggestedDeveloperFieldId;

    /** ID da opção do user dentro do field acima (match por username). Null se não detectou. */
    private String suggestedDeveloperOptionId;

    /** ID do field detectado como "Ordem" (match por nome). Null se não detectou. */
    private String suggestedOrderFieldId;

    /** ID do field detectado como "Branch" (match por nome). Null se não detectou. */
    private String suggestedBranchFieldId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Field {
        private String id;
        private String name;
        /** Ex: "drop_down", "number", "text". */
        private String type;
        /** Opcional — preenchido só pra drop_down. */
        private List<Option> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String id;
        private String name;
    }
}
