package com.planejamais.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "registros_estudo_diario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "data"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEstudoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    @Builder.Default
    private int sessoes = 0;
}
