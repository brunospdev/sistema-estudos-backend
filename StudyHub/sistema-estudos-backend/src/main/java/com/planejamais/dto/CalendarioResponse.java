package com.planejamais.dto;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.domain.TipoMarco;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CalendarioResponse {
    private List<DiaCalendario> dias;
    private MarcoResumoResponse marcoPrincipal;

    @Data
    @AllArgsConstructor
    public static class DiaCalendario {
        private LocalDate data;
        private List<ItemCalendario> itens;
        private List<MarcoCalendario> marcos;
    }

    @Data
    @AllArgsConstructor
    public static class ItemCalendario {
        private Long id;
        private String nome;
        private String disciplinaNome;
        private StatusEstudo status;
        private TipoItem tipo;
    }

    @Data
    @AllArgsConstructor
    public static class MarcoCalendario {
        private Long id;
        private TipoMarco tipo;
        private String titulo;
        private boolean ehPrincipal;
    }
}
