package com.planejamais.dto;

import com.planejamais.domain.TipoMarco;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MarcoResponse {
    private Long id;
    private TipoMarco tipo;
    private String titulo;
    private LocalDate data;
    private String notas;
    private Long disciplinaId;
    private String disciplinaNome;
    private boolean ehPrincipal;
    private LocalDateTime createdAt;
}
