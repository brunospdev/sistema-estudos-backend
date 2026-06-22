package com.planejamais.dto;

import com.planejamais.domain.FrequenciaRecorrencia;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RecorrenciaRequest {

    @NotNull(message = "Frequência é obrigatória.")
    private FrequenciaRecorrencia frequencia;

    @Min(value = 1, message = "Intervalo deve ser no mínimo 1.")
    private Integer intervalo = 1;

    /** 1=segunda … 7=domingo (ISO-8601) */
    private List<Integer> diasSemana;

    private Integer diaMes;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    @Min(value = 1, message = "Mínimo de 1 ocorrência.")
    private Integer maxOcorrencias;
}
