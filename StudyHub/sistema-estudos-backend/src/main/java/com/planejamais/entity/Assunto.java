package com.planejamais.entity;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assuntos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Assunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoItem tipo = TipoItem.CONTEUDO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_estudo", nullable = false)
    @Builder.Default
    private StatusEstudo statusEstudo = StatusEstudo.NAO_INICIADO;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "horas_acumuladas", nullable = false)
    @Builder.Default
    private BigDecimal horasAcumuladas = BigDecimal.ZERO;

    @Column(name = "ultima_sessao_em")
    private LocalDateTime ultimaSessaoEm;

    @Column(name = "data_programada")
    private LocalDate dataProgramada;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "data_realizada")
    private LocalDate dataRealizada;

    @Column(precision = 8, scale = 2)
    private BigDecimal nota;

    @Column(name = "nota_maxima", precision = 8, scale = 2)
    private BigDecimal notaMaxima;

    @Column(name = "entrega_concluida", nullable = false)
    @Builder.Default
    private boolean entregaConcluida = false;

    @OneToMany(mappedBy = "assunto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private java.util.List<AssuntoDescricao> descricoes = new java.util.ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private Assunto parentItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

}
