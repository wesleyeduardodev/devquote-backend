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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Group {
        private String status;
        private boolean primary;
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
    }
}
