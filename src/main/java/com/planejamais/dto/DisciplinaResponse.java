package com.planejamais.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DisciplinaResponse {
    private Long id;
    private String nome;
    private LocalDateTime dataCriacao;
}
