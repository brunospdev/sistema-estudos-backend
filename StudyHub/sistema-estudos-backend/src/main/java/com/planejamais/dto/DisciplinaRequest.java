package com.planejamais.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisciplinaRequest {

    @NotBlank(message = "Nome da disciplina é obrigatório.")
    private String nome;

    private Long parentId;
}
