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

    private Long quoteId;
    
    private String taskName;
    
    private String taskCode;
    
    private String quoteStatus;
    
    private BigDecimal quoteValue;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer totalDeliveries;
    
    private Integer completedDeliveries;
    
    private Integer pendingDeliveries;
    
    private List<DeliveryResponse> deliveries;
}