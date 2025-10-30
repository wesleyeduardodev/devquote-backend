package br.com.devquote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_operational_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOperationalAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_operational_item_id", nullable = false)
    private DeliveryOperationalItem deliveryOperationalItem;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreateAttachment() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
