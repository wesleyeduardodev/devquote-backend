package br.com.devquote.entity;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.enums.Environment;
import br.com.devquote.enums.FlowType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

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

    @Column(name = "environment", length = 30)
    @Enumerated(EnumType.STRING)
    private Environment environment;

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

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

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

    public DeliveryStatus calculateStatus() {
        List<DeliveryStatus> itemStatuses = new ArrayList<>();

        if (items != null && !items.isEmpty()) {
            itemStatuses.addAll(items.stream()
                    .map(DeliveryItem::getStatus)
                    .toList());
        }

        if (operationalItems != null && !operationalItems.isEmpty()) {
            itemStatuses.addAll(operationalItems.stream()
                    .map(item -> {
                        switch (item.getStatus()) {
                            case PENDING -> {
                                return DeliveryStatus.PENDING;
                            }
                            case DELIVERED -> {
                                return DeliveryStatus.DELIVERED;
                            }
                            case CANCELLED -> {
                                return DeliveryStatus.CANCELLED;
                            }
                            default -> {
                                return DeliveryStatus.PENDING;
                            }
                        }
                    })
                    .toList());
        }

        if (itemStatuses.isEmpty()) {
            return DeliveryStatus.PENDING;
        }

        itemStatuses = itemStatuses.stream().distinct().toList();

        if (itemStatuses.size() == 1) {
            return itemStatuses.get(0);
        }

        if (itemStatuses.contains(DeliveryStatus.CANCELLED)) {
            return DeliveryStatus.CANCELLED;
        }

        if (itemStatuses.contains(DeliveryStatus.DEVELOPMENT)) {
            return DeliveryStatus.DEVELOPMENT;
        }

        if (itemStatuses.contains(DeliveryStatus.PENDING)) {
            return DeliveryStatus.PENDING;
        }

        if (itemStatuses.contains(DeliveryStatus.REJECTED)) {
            return DeliveryStatus.REJECTED;
        }

        if (itemStatuses.contains(DeliveryStatus.HOMOLOGATION)) {
            return DeliveryStatus.HOMOLOGATION;
        }

        if (itemStatuses.contains(DeliveryStatus.DELIVERED)) {
            return DeliveryStatus.DELIVERED;
        }

        if (itemStatuses.contains(DeliveryStatus.APPROVED)) {
            return DeliveryStatus.APPROVED;
        }
        if (itemStatuses.contains(DeliveryStatus.PRODUCTION)) {
            return DeliveryStatus.PRODUCTION;
        }

        return DeliveryStatus.PENDING;
    }

    public void updateStatus() {
        this.status = calculateStatus();
    }

    public void updateDates() {
        if (this.flowType == FlowType.DESENVOLVIMENTO) {
            if (items != null && !items.isEmpty()) {
                this.startedAt = items.stream()
                    .map(DeliveryItem::getStartedAt)
                    .filter(date -> date != null)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

                this.finishedAt = items.stream()
                    .map(DeliveryItem::getFinishedAt)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            } else {
                this.startedAt = null;
                this.finishedAt = null;
            }
        } else if (this.flowType == FlowType.OPERACIONAL) {
            if (operationalItems != null && !operationalItems.isEmpty()) {
                this.startedAt = operationalItems.stream()
                    .map(DeliveryOperationalItem::getStartedAt)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);

                this.finishedAt = operationalItems.stream()
                    .map(DeliveryOperationalItem::getFinishedAt)
                    .filter(date -> date != null)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            } else {
                this.startedAt = null;
                this.finishedAt = null;
            }
        }
    }

    public int getTotalItems() {
        int devItems = items != null ? items.size() : 0;
        int opItems = operationalItems != null ? operationalItems.size() : 0;
        return devItems + opItems;
    }

    public long getItemsByStatus(DeliveryStatus status) {
        long devItemsCount = items != null ? items.stream().filter(item -> item.getStatus() == status).count() : 0;

        long opItemsCount = 0;
        if (operationalItems != null) {
            opItemsCount = operationalItems.stream()
                .filter(item -> {
                    DeliveryStatus mappedStatus = switch (item.getStatus()) {
                        case PENDING -> DeliveryStatus.PENDING;
                        case DELIVERED -> DeliveryStatus.DELIVERED;
                        case CANCELLED -> DeliveryStatus.CANCELLED;
                    };
                    return mappedStatus == status;
                })
                .count();
        }

        return devItemsCount + opItemsCount;
    }

    public void addItem(DeliveryItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setDelivery(this);
        updateStatus();
    }

    public void removeItem(DeliveryItem item) {
        if (items != null) {
            items.remove(item);
            item.setDelivery(null);
            updateStatus();
        }
    }

    public void removeOperationalItem(DeliveryOperationalItem item) {
        if (operationalItems != null) {
            operationalItems.remove(item);
            item.setDelivery(null);
            updateStatus();
        }
    }
}
