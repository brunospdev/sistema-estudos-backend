package com.planejamais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DescricaoRequest {

    @NotBlank(message = "Texto é obrigatório.")
    @Size(max = 2000)
    private String texto;
}
