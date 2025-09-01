package br.com.devquote.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billing_period",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_billing_period_year_month",
                        columnNames = {"year", "month"}
                )
        },
        indexes = {
                @Index(name = "idx_bp_year_month", columnList = "year, month")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(length = 30)
    private String status;

    @Column(name = "billing_email_sent")
    @Builder.Default
    private Boolean billingEmailSent = false;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}