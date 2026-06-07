package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class PomodoroResponse {
    private Map<String, Integer> heatmap;
    private DuracoesPomodoro duracoes;
    private long ciclosConcluidos;

    @Data
    @AllArgsConstructor
    public static class DuracoesPomodoro {
        private int foco;
        private int curto;
        private int longo;
    }
}
