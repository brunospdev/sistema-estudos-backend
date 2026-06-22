package com.planejamais.dto;

import com.planejamais.domain.LayoutMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreferenciasRequest {
    @NotNull
    private LayoutMode layoutMode;

    @NotBlank
    @Size(max = 40)
    private String labelGrupo;

    @NotBlank
    @Size(max = 40)
    private String labelItem;

    private BigDecimal metaHorasDiarias;

    private Integer profundidadeGrupos;

    @Size(max = 40)
    private String labelGrupoNivel2;

    @Size(max = 40)
    private String presetPersona;
}
