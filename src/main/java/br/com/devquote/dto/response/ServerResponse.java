package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServerResponse implements Serializable {

    private Long id;

    private String name;

    private String link;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
