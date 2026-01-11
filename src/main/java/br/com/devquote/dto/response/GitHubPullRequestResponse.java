package br.com.devquote.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequestResponse {

    private Long id;
    private Integer number;
    private String state;
    private String title;
    private Boolean merged;

    @JsonProperty("merged_at")
    private String mergedAt;

    @JsonProperty("html_url")
    private String htmlUrl;
}
