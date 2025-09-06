package br.com.devquote.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryResponse {

    private Long id;
    private Long taskId;
    private String taskName;
    private String taskCode;
    private String status;
    private Integer totalItems;
    private Long pendingCount;
    private Long developmentCount;
    private Long deliveredCount;
    private Long homologationCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long productionCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Boolean deliveryEmailSent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Lista de itens da entrega
    private java.util.List<DeliveryItemResponse> items;
}
