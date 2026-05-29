package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityBoardResponse {

    private String provider;
    private boolean configured;
    private String fetchedAt;
    private List<Group> groups;

    /** User dono do token configurado — útil pra UI mostrar "Conectado como X". Null quando não detectado. */
    private CurrentUser currentUser;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUser {
        private String id;
        private String username;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Group {
        private String status;
        private boolean primary;
        /** Marcado como hidden via CLICKUP_HIDDEN_STATUSES — não some, só sinaliza pra UI esconder por padrão. */
        private boolean hidden;
        private int count;
        private List<Task> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        private String id;
        private String name;
        private String description;
        private String url;
        private Double ordem;
        private String priority;
        private String type;
        private List<String> tags;
        private boolean existsInDevQuote;
        /** Id interno da Task no DevQuote quando já cadastrada (casado por code = id do ClickUp); null caso contrário. */
        private Long devQuoteTaskId;
    }
}
