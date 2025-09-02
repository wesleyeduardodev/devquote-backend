package br.com.devquote.entity;

import br.com.devquote.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "delivery_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "branch", length = 200)
    private String branch;

    @Column(name = "source_branch", length = 200)
    private String sourceBranch;

    @Column(name = "pull_request", length = 500)
    private String pullRequest;

    @Lob
    @Column(name = "script")
    private String script;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;
}