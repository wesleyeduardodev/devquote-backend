package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryStatusCount {
    
    private Integer pending;
    private Integer development;
    private Integer delivered;
    private Integer homologation;
    private Integer approved;
    private Integer rejected;
    private Integer production;

    public Integer getTotal() {
        return (pending != null ? pending : 0) +
               (development != null ? development : 0) +
               (delivered != null ? delivered : 0) +
               (homologation != null ? homologation : 0) +
               (approved != null ? approved : 0) +
               (rejected != null ? rejected : 0) +
               (production != null ? production : 0);
    }

    public Integer getCompleted() {
        return (approved != null ? approved : 0) +
               (production != null ? production : 0);
    }

    public Integer getPendingCount() {
        return getTotal() - getCompleted();
    }
}