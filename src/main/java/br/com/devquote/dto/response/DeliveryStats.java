package br.com.devquote.dto.response;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryStats {

    private long total;
    private long totalPending;
    private long totalDevelopment;
    private long totalDelivered;
    private long totalHomologation;
    private long totalApproved;
    private long totalRejected;
    private long totalProduction;
    private long totalCancelled;
    private long totalWithoutItems;
}
