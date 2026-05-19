package br.com.devquote.dto.response;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryStats {

    private long total;
    private long totalPending;
    private long totalInProgress;
    private long totalRejected;
    private long totalProduction;
    private long totalWithoutItems;
}
