package com.planejamais.entity;

import com.planejamais.domain.FrequenciaRecorrencia;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recorrencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequenciaRecorrencia frequencia;

    @Column(nullable = false)
    @Builder.Default
    private int intervalo = 1;

    @Column(name = "dias_semana")
    private String diasSemana;

    @Column(name = "dia_mes")
    private Integer diaMes;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "max_ocorrencias")
    private Integer maxOcorrencias;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativa = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
