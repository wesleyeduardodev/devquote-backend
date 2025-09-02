package br.com.devquote.entity;
import br.com.devquote.enums.DeliveryStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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
        if (items == null || items.isEmpty()) {
            return DeliveryStatus.PENDING;
        }

        List<DeliveryStatus> itemStatuses = items.stream()
                .map(DeliveryItem::getStatus)
                .distinct()
                .toList();

        // Se todos os itens têm o mesmo status, a entrega tem esse status
        if (itemStatuses.size() == 1) {
            return itemStatuses.get(0);
        }

        // Se há status mistos, verificamos a prioridade (maior progresso)
        if (itemStatuses.contains(DeliveryStatus.PRODUCTION)) {
            return DeliveryStatus.PRODUCTION;
        }
        if (itemStatuses.contains(DeliveryStatus.APPROVED)) {
            return DeliveryStatus.APPROVED;
        }
        if (itemStatuses.contains(DeliveryStatus.DELIVERED)) {
            return DeliveryStatus.DELIVERED;
        }
        if (itemStatuses.contains(DeliveryStatus.HOMOLOGATION)) {
            return DeliveryStatus.HOMOLOGATION;
        }
        if (itemStatuses.contains(DeliveryStatus.DEVELOPMENT)) {
            return DeliveryStatus.DEVELOPMENT;
        }
        if (itemStatuses.contains(DeliveryStatus.REJECTED)) {
            return DeliveryStatus.REJECTED;
        }

        return DeliveryStatus.PENDING;
    }

    // Método para atualizar o status automaticamente
    public void updateStatus() {
        this.status = calculateStatus();
    }
    
    // Método helper para contagens
    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }
    
    public long getItemsByStatus(DeliveryStatus status) {
        if (items == null) return 0;
        return items.stream().filter(item -> item.getStatus() == status).count();
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
}
