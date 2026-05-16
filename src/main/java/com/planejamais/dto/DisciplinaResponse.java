package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DisciplinaResponse {
    private Long id;
    private String nome;
    private LocalDateTime dataCriacao;
    private List<TopicoResponse> topicos;

    @Data
    @AllArgsConstructor
    public static class TopicoResponse {
        private Long id;
        private String nome;
        private boolean concluido;
    }
}
