package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemParameterResponse {

    private Long id;
    private String name;
    private String value;
    private String description;
    private String createdAt;
    private String updatedAt;
}
