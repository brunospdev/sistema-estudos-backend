package com.planejamais.dto;

import com.planejamais.domain.LayoutMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciasResponse {
    private LayoutMode layoutMode;
    private String labelGrupo;
    private String labelItem;
    private BigDecimal metaHorasDiarias;
    private int profundidadeGrupos;
    private String labelGrupoNivel2;
    private String presetPersona;
    private MarcoResumoResponse marcoPrincipal;
}
