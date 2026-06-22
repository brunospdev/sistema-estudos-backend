package com.planejamais.service;

import com.planejamais.domain.LayoutMode;
import com.planejamais.domain.StatusEstudo;
import com.planejamais.domain.TipoItem;
import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EstudoService extends BaseService {

    private static final int HEATMAP_DIAS = 84;

    private final RegistroEstudoDiarioRepository registroRepository;
    private final ConfiguracaoPomodoroRepository configRepository;
    private final SessaoEstudoRepository sessaoRepository;
    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final EventoStatusRepository eventoStatusRepository;

    public EstudoService(UsuarioRepository usuarioRepository,
                         RegistroEstudoDiarioRepository registroRepository,
                         ConfiguracaoPomodoroRepository configRepository,
                         SessaoEstudoRepository sessaoRepository,
                         AssuntoRepository assuntoRepository,
                         DisciplinaRepository disciplinaRepository,
                         EventoStatusRepository eventoStatusRepository) {
        super(usuarioRepository);
        this.registroRepository = registroRepository;
        this.configRepository = configRepository;
        this.sessaoRepository = sessaoRepository;
        this.assuntoRepository = assuntoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.eventoStatusRepository = eventoStatusRepository;
    }

    private ConfiguracaoPomodoro getOuCriarConfig(Usuario usuario) {
        return configRepository.findByUsuario(usuario)
                .orElseGet(() -> configRepository.save(
                        ConfiguracaoPomodoro.builder().usuario(usuario).build()
                ));
    }

    public PomodoroResponse obterPomodoro(String email) {
        Usuario usuario = getUsuario(email);
        ConfiguracaoPomodoro config = getOuCriarConfig(usuario);

        LocalDate inicio = LocalDate.now().minusDays(HEATMAP_DIAS - 1L);
        Map<String, Integer> heatmap = new LinkedHashMap<>();

        registroRepository.findByUsuario_IdAndDataGreaterThanEqual(usuario.getId(), inicio)
                .forEach(registro -> heatmap.put(registro.getData().toString(), registro.getSessoes()));

        long ciclosConcluidos = registroRepository.somarSessoesPorUsuario(usuario.getId());

        return new PomodoroResponse(
                heatmap,
                new PomodoroResponse.DuracoesPomodoro(
                        config.getMinutosFoco(),
                        config.getMinutosPausaCurta(),
                        config.getMinutosPausaLonga()
                ),
                ciclosConcluidos
        );
    }

    @Transactional
    public SessaoEstudoResponse registrarSessao(String email, SessaoEstudoRequest request) {
        Usuario usuario = getUsuario(email);

        if (request.getFimEm().isBefore(request.getInicioEm())) {
            throw new IllegalArgumentException("fimEm deve ser posterior a inicioEm.");
        }

        int duracaoMinutos = (int) Duration.between(request.getInicioEm(), request.getFimEm()).toMinutes();
        if (duracaoMinutos <= 0) {
            duracaoMinutos = 1;
        }

        Assunto assunto = null;
        Disciplina disciplina = null;

        if (request.getAssuntoId() != null) {
            assunto = assuntoRepository.findByIdAndDisciplina_Usuario_Id(request.getAssuntoId(), usuario.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assunto não encontrado."));
            if (!isTipoEstudavel(assunto.getTipo())) {
                throw new IllegalArgumentException("Sessões de estudo só podem ser vinculadas a itens de conteúdo, revisão ou prática.");
            }
            disciplina = assunto.getDisciplina();
        } else if (request.getDisciplinaId() != null) {
            disciplina = disciplinaRepository.findByIdAndUsuario(request.getDisciplinaId(), usuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        }

        sessaoRepository.save(SessaoEstudo.builder()
                .usuario(usuario)
                .assunto(assunto)
                .disciplina(disciplina)
                .inicioEm(request.getInicioEm())
                .fimEm(request.getFimEm())
                .duracaoMinutos(duracaoMinutos)
                .dificuldade(request.getDificuldade())
                .anotacao(request.getAnotacao())
                .build());

        if (assunto != null) {
            BigDecimal horasNovas = assunto.getHorasAcumuladas().add(
                    BigDecimal.valueOf(duracaoMinutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            assunto.setHorasAcumuladas(horasNovas);
            assunto.setUltimaSessaoEm(request.getFimEm());

            if (assunto.getStatusEstudo() == StatusEstudo.NAO_INICIADO) {
                StatusEstudo anterior = assunto.getStatusEstudo();
                assunto.setStatusEstudo(StatusEstudo.EM_ANDAMENTO);
                eventoStatusRepository.save(EventoStatus.builder()
                        .usuario(usuario)
                        .assunto(assunto)
                        .statusAnterior(anterior)
                        .statusNovo(StatusEstudo.EM_ANDAMENTO)
                        .build());
            }

            assuntoRepository.save(assunto);
        }

        LocalDate dataSessao = request.getFimEm().toLocalDate();
        RegistroEstudoDiario registro = registroRepository.findByUsuarioAndData(usuario, dataSessao)
                .orElseGet(() -> RegistroEstudoDiario.builder()
                        .usuario(usuario)
                        .data(dataSessao)
                        .sessoes(0)
                        .build());

        registro.setSessoes(registro.getSessoes() + 1);
        registroRepository.save(registro);

        long ciclosConcluidos = registroRepository.somarSessoesPorUsuario(usuario.getId());
        return new SessaoEstudoResponse(dataSessao.toString(), registro.getSessoes(), ciclosConcluidos);
    }

    @Transactional
    public SessaoEstudoResponse registrarSessao(String email) {
        LocalDateTime fim = LocalDateTime.now();
        SessaoEstudoRequest request = new SessaoEstudoRequest();
        request.setInicioEm(fim.minusMinutes(25));
        request.setFimEm(fim);
        return registrarSessao(email, request);
    }

    @Transactional
    public PomodoroResponse salvarConfig(String email, PomodoroConfigRequest request) {
        Usuario usuario = getUsuario(email);
        ConfiguracaoPomodoro config = getOuCriarConfig(usuario);

        config.setMinutosFoco(request.getFoco());
        config.setMinutosPausaCurta(request.getCurto());
        config.setMinutosPausaLonga(request.getLongo());
        configRepository.save(config);

        return obterPomodoro(email);
    }

    private boolean isTipoEstudavel(TipoItem tipo) {
        return tipo == TipoItem.CONTEUDO || tipo == TipoItem.REVISAO || tipo == TipoItem.PRATICA;
    }
}
