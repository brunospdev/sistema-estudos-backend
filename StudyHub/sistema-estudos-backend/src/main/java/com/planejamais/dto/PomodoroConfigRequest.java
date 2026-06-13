package com.planejamais.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PomodoroConfigRequest {

    @Min(value = 1, message = "Tempo de foco deve ser no mínimo 1 minuto.")
    @Max(value = 180, message = "Tempo de foco deve ser no máximo 180 minutos.")
    private int foco;

    @Min(value = 1, message = "Pausa curta deve ser no mínimo 1 minuto.")
    @Max(value = 60, message = "Pausa curta deve ser no máximo 60 minutos.")
    private int curto;

    @Min(value = 1, message = "Pausa longa deve ser no mínimo 1 minuto.")
    @Max(value = 120, message = "Pausa longa deve ser no máximo 120 minutos.")
    private int longo;
}
