package com.planejamais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssuntoRequest {

    @NotBlank(message = "Título é obrigatório.")
    private String titulo;

    @NotNull(message = "Data programada é obrigatória.")
    private LocalDate dataProgramada;

    @NotNull(message = "Disciplina é obrigatória.")
    private Long disciplinaId;
}
