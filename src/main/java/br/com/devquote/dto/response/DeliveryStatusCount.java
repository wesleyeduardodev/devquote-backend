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
    
    // Método helper para obter o total
    public Integer getTotal() {
        return (pending != null ? pending : 0) +
               (development != null ? development : 0) +
               (delivered != null ? delivered : 0) +
               (homologation != null ? homologation : 0) +
               (approved != null ? approved : 0) +
               (rejected != null ? rejected : 0) +
               (production != null ? production : 0);
    }
    
    // Método helper para obter entregas finalizadas (APPROVED e PRODUCTION)
    public Integer getCompleted() {
        return (approved != null ? approved : 0) +
               (production != null ? production : 0);
    }
    
    // Método helper para obter entregas pendentes (não finalizadas)
    public Integer getPendingCount() {
        return getTotal() - getCompleted();
    }
}