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

    private String taskType;

    private String deliveryStatus;

    private BigDecimal taskValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer totalItems;

    private String calculatedDeliveryStatus;

    private DeliveryStatusCount statusCounts;

    private Integer totalDeliveries;

    private Integer completedDeliveries;

    private Integer pendingDeliveries;

    private List<DeliveryResponse> deliveries;

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
