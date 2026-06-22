package com.planejamais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TopicoUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String nome;

    private LocalDate dataProgramada;
}
