package com.planejamais.dto;

import com.planejamais.domain.TipoItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TopicoCreateRequest {

    @NotBlank(message = "Nome é obrigatório.")
    private String nome;

    private TipoItem tipo;

    private LocalDate dataProgramada;

    private LocalDate dataEntrega;

    private BigDecimal notaMaxima;

    private String descricao;

    private Long parentItemId;

    private RecorrenciaRequest recorrencia;
}
