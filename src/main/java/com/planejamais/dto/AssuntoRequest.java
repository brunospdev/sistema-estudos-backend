package com.planejamais.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AssuntoRequest {
    private String titulo;
    private LocalDate dataProgramada;
    private Long disciplinaId;
}
