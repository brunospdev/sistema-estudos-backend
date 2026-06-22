package com.planejamais.entity;

import com.planejamais.domain.StatusEstudo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private StatusEstudo statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusEstudo statusNovo;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm;

    @PrePersist
    protected void onCreate() {
        this.ocorridoEm = LocalDateTime.now();
    }
}
