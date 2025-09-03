package br.com.devquote.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryGroupResponse {

    private Long taskId;

    private String taskName;

    private String taskCode;

    private String deliveryStatus;

    private BigDecimal taskValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Novos campos para exibição na listagem
    private Integer totalItems;

    private String calculatedDeliveryStatus;

    // Novos campos com contadores por status
    private DeliveryStatusCount statusCounts;

    // Campos compatíveis para backward compatibility
    private Integer totalDeliveries;

    private Integer completedDeliveries;

    private Integer pendingDeliveries;

    private List<DeliveryResponse> deliveries;
    
    // Métodos helpers para backward compatibility
    public Integer getTotalDeliveries() {
        return statusCounts != null ? statusCounts.getTotal() : totalDeliveries;
    }
    
    public Integer getCompletedDeliveries() {
        return statusCounts != null ? statusCounts.getCompleted() : completedDeliveries;
    }
    
    public Integer getPendingDeliveries() {
        return statusCounts != null ? statusCounts.getPendingCount() : pendingDeliveries;
    }
}
