package com.planejamais.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracoes_pomodoro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoPomodoro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    @Builder.Default
    private int minutosFoco = 25;

    @Column(nullable = false)
    @Builder.Default
    private int minutosPausaCurta = 5;

    @Column(nullable = false)
    @Builder.Default
    private int minutosPausaLonga = 15;
}
