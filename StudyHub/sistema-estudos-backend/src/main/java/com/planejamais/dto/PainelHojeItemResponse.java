package com.planejamais.dto;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PainelHojeItemResponse {
    private Long id;
    private String titulo;
    private TipoItem tipo;
    private StatusEstudo status;
    private LocalDate dataProgramada;
    private LocalDate dataEntrega;
    private Long disciplinaId;
    private String disciplinaNome;
    private boolean atrasado;
    private boolean entrega;
    private LocalDateTime ultimaSessaoEm;
}
