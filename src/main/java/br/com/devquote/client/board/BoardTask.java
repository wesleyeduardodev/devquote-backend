package br.com.devquote.client.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modelo agnóstico de tarefa de um board externo (ClickUp, Jira, ...).
 * Cada provider mapeia sua resposta crua para este formato.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardTask {

    private String id;
    private String name;
    private String description;
    private String url;
    private String statusName;
    private Double orderValue;
    private String priority;
    private String type;
    private List<String> tags;
}
