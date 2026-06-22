package com.planejamais.dto;

import com.planejamais.domain.FrequenciaRecorrencia;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class RecorrenciaResponse {
    private Long id;
    private FrequenciaRecorrencia frequencia;
    private Integer intervalo;
    private List<Integer> diasSemana;
    private Integer diaMes;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer maxOcorrencias;
    private boolean ativa;
    private Integer indiceOcorrencia;
}
