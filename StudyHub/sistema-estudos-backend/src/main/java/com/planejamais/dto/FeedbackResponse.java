package com.planejamais.dto;

import com.planejamais.domain.StatusEstudo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FeedbackResponse {
    private List<MensagemFeedback> mensagens;
    private ProximoSugerido proximoSugerido;
    private MetaDiaria metaDiaria;
    private int streak;

    @Data
    @AllArgsConstructor
    public static class MensagemFeedback {
        private String tipo;
        private String texto;
        private int prioridade;
        private Long assuntoId;
    }

    @Data
    @AllArgsConstructor
    public static class ProximoSugerido {
        private Long assuntoId;
        private String nome;
        private String motivo;
    }

    @Data
    @AllArgsConstructor
    public static class MetaDiaria {
        private Integer metaMinutos;
        private long realizadoMinutos;
        private int percentual;
    }
}
