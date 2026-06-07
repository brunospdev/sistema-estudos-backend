package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AssuntoResponse {
    private Long id;
    private String titulo;
    private boolean status;
    private LocalDate dataProgramada;
    private LocalDate dataConclusao;
    private Long disciplinaId;
    private String disciplinaNome;
}
