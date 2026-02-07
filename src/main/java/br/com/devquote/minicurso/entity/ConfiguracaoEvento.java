package br.com.devquote.minicurso.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "configuracao_evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 200)
    private String local;

    @Column(name = "quantidade_vagas")
    private Integer quantidadeVagas;

    @Column(name = "inscricoes_abertas", nullable = false)
    @Builder.Default
    private Boolean inscricoesAbertas = true;

    @Column(name = "exibir_fale_conosco", nullable = false)
    @Builder.Default
    private Boolean exibirFaleConosco = false;

    @Column(name = "email_contato", length = 150)
    private String emailContato;

    @Column(name = "whatsapp_contato", length = 20)
    private String whatsappContato;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "configuracaoEvento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<DataEvento> datasEvento = new ArrayList<>();

    @OneToMany(mappedBy = "configuracaoEvento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<ModuloEvento> modulos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
