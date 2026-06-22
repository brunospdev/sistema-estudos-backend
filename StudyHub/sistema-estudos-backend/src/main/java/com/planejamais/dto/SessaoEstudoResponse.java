package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessaoEstudoResponse {
    private String data;
    private int sessoes;
    private long ciclosConcluidos;
}
