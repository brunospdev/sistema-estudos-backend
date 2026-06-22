package com.planejamais.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TopicoAvaliacaoRequest {

    private BigDecimal nota;

    private BigDecimal notaMaxima;

    private LocalDate dataRealizada;

    private Boolean entregaConcluida;
}
