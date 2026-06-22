package com.planejamais.dto;

import com.planejamais.domain.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EventoResponse {
    private Long id;
    private String nome;
    private TipoItem tipo;
    private java.util.List<String> descricoes;
    private LocalDate dataEntrega;
    private LocalDate dataRealizada;
    private BigDecimal nota;
    private BigDecimal notaMaxima;
    private boolean concluido;
    private Long disciplinaId;
    private String disciplinaNome;
}
