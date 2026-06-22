package com.planejamais.dto;

import com.planejamais.domain.TipoMarco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MarcoRequest {
    @NotNull
    private TipoMarco tipo;

    @NotBlank
    @Size(max = 255)
    private String titulo;

    @NotNull
    private LocalDate data;

    private String notas;
    private Long disciplinaId;
    private Boolean ehPrincipal;
}
