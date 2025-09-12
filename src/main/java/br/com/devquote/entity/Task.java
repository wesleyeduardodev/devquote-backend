package br.com.devquote.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "task", uniqueConstraints = {
    @UniqueConstraint(name = "uk_task_code", columnNames = "code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Requester requester;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(length = 200)
    private String link;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "notes", length = 256)
    private String notes;

    @Column(name = "has_sub_tasks", nullable = false)
    @Builder.Default
    private Boolean hasSubTasks = false;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "task_type", length = 50)
    private String taskType; // BUG, ENHANCEMENT, NEW_FEATURE

    @Column(name = "server_origin", length = 100)
    private String serverOrigin;

    @Column(name = "system_module", length = 100)
    private String systemModule;

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "financial_email_sent")
    @Builder.Default
    private Boolean financialEmailSent = false;

    @Column(name = "task_email_sent")
    @Builder.Default
    private Boolean taskEmailSent = false;

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

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskAttachment> attachments;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
