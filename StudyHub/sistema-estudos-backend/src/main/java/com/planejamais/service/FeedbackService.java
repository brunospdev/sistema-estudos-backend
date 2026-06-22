package com.planejamais.service;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.dto.FeedbackResponse;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.MarcoUsuario;
import com.planejamais.entity.PreferenciasUsuario;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.MarcoUsuarioRepository;
import com.planejamais.repository.PreferenciasUsuarioRepository;
import com.planejamais.repository.RegistroEstudoDiarioRepository;
import com.planejamais.repository.SessaoEstudoRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackService extends BaseService {

    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final AssuntoRepository assuntoRepository;
    private final MarcoUsuarioRepository marcoRepository;
    private final SessaoEstudoRepository sessaoRepository;
    private final RegistroEstudoDiarioRepository registroRepository;

    public FeedbackService(UsuarioRepository usuarioRepository,
                           PreferenciasUsuarioRepository preferenciasRepository,
                           AssuntoRepository assuntoRepository,
                           MarcoUsuarioRepository marcoRepository,
                           SessaoEstudoRepository sessaoRepository,
                           RegistroEstudoDiarioRepository registroRepository) {
        super(usuarioRepository);
        this.preferenciasRepository = preferenciasRepository;
        this.assuntoRepository = assuntoRepository;
        this.marcoRepository = marcoRepository;
        this.sessaoRepository = sessaoRepository;
        this.registroRepository = registroRepository;
    }

    @Transactional(readOnly = true)
    public FeedbackResponse obter(String email) {
        Usuario usuario = getUsuario(email);
        LocalDate hoje = LocalDate.now();

        PreferenciasUsuario prefs = preferenciasRepository.findByUsuario(usuario).orElse(null);
        BigDecimal metaHoras = prefs != null ? prefs.getMetaHorasDiarias() : null;

        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1).atStartOfDay();
        long realizadoMinutos = sessaoRepository.somarMinutosPorPeriodo(usuario, inicioHoje, fimHoje);

        FeedbackResponse.MetaDiaria metaDiaria = null;
        List<FeedbackResponse.MensagemFeedback> mensagens = new ArrayList<>();

        if (metaHoras != null && metaHoras.compareTo(BigDecimal.ZERO) > 0) {
            int metaMinutos = metaHoras.multiply(BigDecimal.valueOf(60)).intValue();
            int percentual = metaMinutos > 0
                    ? (int) Math.min(100, (realizadoMinutos * 100) / metaMinutos)
                    : 0;
            metaDiaria = new FeedbackResponse.MetaDiaria(metaMinutos, realizadoMinutos, percentual);

            if (realizadoMinutos < metaMinutos) {
                long faltam = metaMinutos - realizadoMinutos;
                mensagens.add(new FeedbackResponse.MensagemFeedback(
                        "META_PARCIAL",
                        "Faltam " + faltam + " min para sua meta de " + metaHoras + "h.",
                        1,
                        null
                ));
            }
        }

        int streak = calcularStreak(usuario, hoje);
        if (streak >= 2) {
            mensagens.add(new FeedbackResponse.MensagemFeedback(
                    "STREAK",
                    "Sequência de " + streak + " dias estudando!",
                    2,
                    null
            ));
        }

        List<MarcoUsuario> proximosMarcos = marcoRepository.findProximos(usuario, hoje);
        for (MarcoUsuario marco : proximosMarcos.stream().limit(2).toList()) {
            long dias = ChronoUnit.DAYS.between(hoje, marco.getData());
            String texto = dias == 0
                    ? marco.getTitulo() + " é hoje."
                    : marco.getTitulo() + " em " + dias + " dias.";
            mensagens.add(new FeedbackResponse.MensagemFeedback(
                    "MARCO_PROXIMO",
                    texto,
                    2,
                    null
            ));
        }

        FeedbackResponse.ProximoSugerido proximoSugerido = calcularProximoSugerido(usuario.getId(), hoje);
        if (proximoSugerido != null) {
            mensagens.add(new FeedbackResponse.MensagemFeedback(
                    "SUGESTAO",
                    proximoSugerido.getNome() + " — " + motivoLabel(proximoSugerido.getMotivo()) + ".",
                    3,
                    proximoSugerido.getAssuntoId()
            ));
        }

        return new FeedbackResponse(mensagens, proximoSugerido, metaDiaria, streak);
    }

    private FeedbackResponse.ProximoSugerido calcularProximoSugerido(Long usuarioId, LocalDate hoje) {
        List<Assunto> hojeList = assuntoRepository.findProgramadosParaHoje(usuarioId, hoje, StatusEstudo.DOMINADO);
        if (!hojeList.isEmpty()) {
            Assunto a = hojeList.get(0);
            return new FeedbackResponse.ProximoSugerido(a.getId(), a.getTitulo(), "PROGRAMADO_HOJE");
        }

        List<Assunto> atrasados = assuntoRepository.findAtrasados(usuarioId, hoje, StatusEstudo.DOMINADO);
        if (!atrasados.isEmpty()) {
            Assunto a = atrasados.get(0);
            return new FeedbackResponse.ProximoSugerido(a.getId(), a.getTitulo(), "ATRASADO");
        }

        List<Assunto> emAndamento = assuntoRepository.findByUsuarioAndStatusEstudoOrderByUltimaSessao(
                usuarioId, StatusEstudo.EM_ANDAMENTO);
        if (!emAndamento.isEmpty()) {
            Assunto a = emAndamento.get(0);
            return new FeedbackResponse.ProximoSugerido(a.getId(), a.getTitulo(), "EM_ANDAMENTO_PARADO");
        }

        List<Assunto> futuros = assuntoRepository.findNaoIniciadosComDataFutura(
                usuarioId, StatusEstudo.NAO_INICIADO, hoje);
        if (!futuros.isEmpty()) {
            Assunto a = futuros.get(0);
            return new FeedbackResponse.ProximoSugerido(a.getId(), a.getTitulo(), "AGENDADO_FUTURO");
        }

        List<Assunto> naoIniciados = assuntoRepository.findByUsuarioAndStatusEstudo(
                usuarioId, StatusEstudo.NAO_INICIADO);
        if (!naoIniciados.isEmpty()) {
            Assunto a = naoIniciados.get(0);
            return new FeedbackResponse.ProximoSugerido(a.getId(), a.getTitulo(), "NAO_INICIADO");
        }

        return null;
    }

    private int calcularStreak(Usuario usuario, LocalDate hoje) {
        int streak = 0;
        LocalDate dia = hoje;
        while (true) {
            var registro = registroRepository.findByUsuarioAndData(usuario, dia);
            if (registro.isEmpty() || registro.get().getSessoes() <= 0) {
                break;
            }
            streak++;
            dia = dia.minusDays(1);
        }
        return streak;
    }

    private String motivoLabel(String motivo) {
        return switch (motivo) {
            case "PROGRAMADO_HOJE" -> "programado para hoje";
            case "ATRASADO" -> "atrasado";
            case "EM_ANDAMENTO_PARADO" -> "em andamento";
            case "AGENDADO_FUTURO" -> "próximo agendado";
            case "NAO_INICIADO" -> "não iniciado";
            default -> motivo;
        };
    }
}
