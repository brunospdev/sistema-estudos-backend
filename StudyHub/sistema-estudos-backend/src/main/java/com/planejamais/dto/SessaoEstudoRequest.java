package com.planejamais.dto;

import com.planejamais.domain.DificuldadeSessao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessaoEstudoRequest {
    private Long assuntoId;
    private Long disciplinaId;

    @NotNull
    private LocalDateTime inicioEm;

    @NotNull
    private LocalDateTime fimEm;

    private DificuldadeSessao dificuldade;
    private String anotacao;
}
