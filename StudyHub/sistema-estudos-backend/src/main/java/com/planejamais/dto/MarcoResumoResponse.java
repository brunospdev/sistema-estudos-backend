package com.planejamais.dto;

import com.planejamais.domain.TipoMarco;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MarcoResumoResponse {
    private Long id;
    private String titulo;
    private LocalDate data;
    private Long diasRestantes;
}
