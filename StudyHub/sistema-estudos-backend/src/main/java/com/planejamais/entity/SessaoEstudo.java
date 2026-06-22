package com.planejamais.entity;

import com.planejamais.domain.DificuldadeSessao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessoes_estudo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessaoEstudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assunto_id")
    private Assunto assunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @Column(name = "inicio_em", nullable = false)
    private LocalDateTime inicioEm;

    @Column(name = "fim_em", nullable = false)
    private LocalDateTime fimEm;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Enumerated(EnumType.STRING)
    private DificuldadeSessao dificuldade;

    @Column(columnDefinition = "TEXT")
    private String anotacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
