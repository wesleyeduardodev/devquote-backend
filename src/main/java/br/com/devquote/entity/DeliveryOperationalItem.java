package br.com.devquote.entity;
import br.com.devquote.enums.OperationalItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_operational_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOperationalItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OperationalItemStatus status;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;

    @OneToMany(mappedBy = "deliveryOperationalItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryOperationalAttachment> attachments = new ArrayList<>();

    public void addAttachment(DeliveryOperationalAttachment attachment) {
        attachments.add(attachment);
        attachment.setDeliveryOperationalItem(this);
    }

    public void removeAttachment(DeliveryOperationalAttachment attachment) {
        attachments.remove(attachment);
        attachment.setDeliveryOperationalItem(null);
    }
}
