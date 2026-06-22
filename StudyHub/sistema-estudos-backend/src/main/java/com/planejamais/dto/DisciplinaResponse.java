package com.planejamais.dto;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
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
    private int sortOrder;
    private boolean oculta;
    private Long parentId;
    private List<TopicoResponse> topicos;
    private List<DisciplinaResponse> subgrupos;

    @Data
    @AllArgsConstructor
    public static class TopicoResponse {
        private Long id;
        private String nome;
        private TipoItem tipo;
        private StatusEstudo status;
        private java.time.LocalDate dataProgramada;
        private java.time.LocalDate dataConclusao;
        private java.time.LocalDate dataEntrega;
        private java.time.LocalDate dataRealizada;
        private java.math.BigDecimal nota;
        private java.math.BigDecimal notaMaxima;
        private boolean entregaConcluida;
        private java.math.BigDecimal horasAcumuladas;
        private int sortOrder;
        private LocalDateTime ultimaSessaoEm;
        private Long parentItemId;
        private java.util.List<DescricaoResponse> descricoes;
        private List<TopicoResponse> subitens;
    }
}
