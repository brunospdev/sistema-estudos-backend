package com.planejamais.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracoes_pomodoro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConfiguracaoPomodoro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "minutos_foco", nullable = false)
    @Builder.Default
    private int minutosFoco = 25;

    @Column(name = "minutos_pausa_curta", nullable = false)
    @Builder.Default
    private int minutosPausaCurta = 5;

    @Column(name = "minutos_pausa_longa", nullable = false)
    @Builder.Default
    private int minutosPausaLonga = 15;
}
