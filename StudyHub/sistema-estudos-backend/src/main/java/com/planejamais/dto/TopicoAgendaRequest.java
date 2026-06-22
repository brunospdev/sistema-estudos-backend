package com.planejamais.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TopicoAgendaRequest {
    private LocalDate dataProgramada;
}
