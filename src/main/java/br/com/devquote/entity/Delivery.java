package br.com.devquote.entity;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.enums.FlowType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "delivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @Column(name = "flow_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private FlowType flowType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryOperationalItem> operationalItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delivery_email_sent")
    @Builder.Default
    private Boolean deliveryEmailSent = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Método para calcular o status automaticamente baseado nos itens
    public DeliveryStatus calculateStatus() {
        List<DeliveryStatus> itemStatuses = new ArrayList<>();

        // Adicionar status dos itens de desenvolvimento
        if (items != null && !items.isEmpty()) {
            itemStatuses.addAll(items.stream()
                    .map(DeliveryItem::getStatus)
                    .toList());
        }

        // Adicionar status dos itens operacionais (mapeando para DeliveryStatus)
        if (operationalItems != null && !operationalItems.isEmpty()) {
            itemStatuses.addAll(operationalItems.stream()
                    .map(item -> {
                        // Mapear OperationalItemStatus para DeliveryStatus
                        switch (item.getStatus()) {
                            case PENDING -> {
                                return DeliveryStatus.PENDING;
                            }
                            case DELIVERED -> {
                                return DeliveryStatus.DELIVERED;
                            }
                            default -> {
                                return DeliveryStatus.PENDING;
                            }
                        }
                    })
                    .toList());
        }

        // Se não há itens, retorna PENDING
        if (itemStatuses.isEmpty()) {
            return DeliveryStatus.PENDING;
        }

        itemStatuses = itemStatuses.stream().distinct().toList();

        // Se todos os itens têm o mesmo status, a entrega tem esse status
        if (itemStatuses.size() == 1) {
            return itemStatuses.get(0);
        }

        // Se há status mistos, verificamos a prioridade (menor progresso prevalece)
        // Se pelo menos um está em desenvolvimento, a entrega fica em desenvolvimento
        if (itemStatuses.contains(DeliveryStatus.DEVELOPMENT)) {
            return DeliveryStatus.DEVELOPMENT;
        }
        // Se pelo menos um está pendente (e não há desenvolvimento), fica pendente
        if (itemStatuses.contains(DeliveryStatus.PENDING)) {
            return DeliveryStatus.PENDING;
        }
        // Se pelo menos um foi rejeitado, fica rejeitado
        if (itemStatuses.contains(DeliveryStatus.REJECTED)) {
            return DeliveryStatus.REJECTED;
        }
        // Se pelo menos um está em homologação, fica em homologação
        if (itemStatuses.contains(DeliveryStatus.HOMOLOGATION)) {
            return DeliveryStatus.HOMOLOGATION;
        }
        // Se pelo menos um está entregue, fica entregue
        if (itemStatuses.contains(DeliveryStatus.DELIVERED)) {
            return DeliveryStatus.DELIVERED;
        }
        // Se chegou aqui, só restam aprovados e produção
        if (itemStatuses.contains(DeliveryStatus.APPROVED)) {
            return DeliveryStatus.APPROVED;
        }
        if (itemStatuses.contains(DeliveryStatus.PRODUCTION)) {
            return DeliveryStatus.PRODUCTION;
        }

        return DeliveryStatus.PENDING;
    }

    // Método para atualizar o status automaticamente
    public void updateStatus() {
        this.status = calculateStatus();
    }
    
    // Método helper para contagens
    public int getTotalItems() {
        int devItems = items != null ? items.size() : 0;
        int opItems = operationalItems != null ? operationalItems.size() : 0;
        return devItems + opItems;
    }

    public long getItemsByStatus(DeliveryStatus status) {
        long devItemsCount = items != null ? items.stream().filter(item -> item.getStatus() == status).count() : 0;

        // Mapear OperationalItemStatus para DeliveryStatus para comparação
        long opItemsCount = 0;
        if (operationalItems != null) {
            opItemsCount = operationalItems.stream()
                .filter(item -> {
                    DeliveryStatus mappedStatus = switch (item.getStatus()) {
                        case PENDING -> DeliveryStatus.PENDING;
                        case DELIVERED -> DeliveryStatus.DELIVERED;
                    };
                    return mappedStatus == status;
                })
                .count();
        }

        return devItemsCount + opItemsCount;
    }

    // Método helper para adicionar item
    public void addItem(DeliveryItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setDelivery(this);
        updateStatus(); // Atualiza status automaticamente
    }

    // Método helper para remover item
    public void removeItem(DeliveryItem item) {
        if (items != null) {
            items.remove(item);
            item.setDelivery(null);
            updateStatus(); // Atualiza status automaticamente
        }
    }

    // Método helper para adicionar item operacional
    public void addOperationalItem(DeliveryOperationalItem item) {
        if (operationalItems == null) {
            operationalItems = new ArrayList<>();
        }
        operationalItems.add(item);
        item.setDelivery(this);
        updateStatus(); // Atualiza status automaticamente
    }

    // Método helper para remover item operacional
    public void removeOperationalItem(DeliveryOperationalItem item) {
        if (operationalItems != null) {
            operationalItems.remove(item);
            item.setDelivery(null);
            updateStatus(); // Atualiza status automaticamente
        }
    }
}
