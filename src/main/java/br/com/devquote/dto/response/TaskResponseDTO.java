package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponseDTO {

    private Long id;

    private Long requesterId;

    private String title;

    private String description;

    private String status;

    private String code;

    private String link;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}