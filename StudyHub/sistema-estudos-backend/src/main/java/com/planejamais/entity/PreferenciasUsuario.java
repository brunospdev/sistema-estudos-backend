package com.planejamais.entity;

import com.planejamais.domain.LayoutMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "preferencias_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciasUsuario {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_mode", nullable = false)
    @Builder.Default
    private LayoutMode layoutMode = LayoutMode.GRUPO_TOPICO;

    @Column(name = "label_grupo", nullable = false)
    @Builder.Default
    private String labelGrupo = "Matéria";

    @Column(name = "label_item", nullable = false)
    @Builder.Default
    private String labelItem = "Tópico";

    @Column(name = "profundidade_grupos", nullable = false)
    @Builder.Default
    private int profundidadeGrupos = 1;

    @Column(name = "label_grupo_nivel2")
    private String labelGrupoNivel2;

    @Column(name = "preset_persona")
    private String presetPersona;

    @Column(name = "meta_horas_diarias")
    private BigDecimal metaHorasDiarias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marco_principal_id")
    private MarcoUsuario marcoPrincipal;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
