package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EstudoService extends BaseService {

    private static final int HEATMAP_DIAS = 84;

    private final RegistroEstudoDiarioRepository registroRepository;
    private final ConfiguracaoPomodoroRepository configRepository;

    public EstudoService(UsuarioRepository usuarioRepository,
                         RegistroEstudoDiarioRepository registroRepository,
                         ConfiguracaoPomodoroRepository configRepository) {
        super(usuarioRepository);
        this.registroRepository = registroRepository;
        this.configRepository = configRepository;
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
    public SessaoEstudoResponse registrarSessao(String email) {
        Usuario usuario = getUsuario(email);
        LocalDate hoje = LocalDate.now();

        RegistroEstudoDiario registro = registroRepository.findByUsuarioAndData(usuario, hoje)
                .orElseGet(() -> RegistroEstudoDiario.builder()
                        .usuario(usuario)
                        .data(hoje)
                        .sessoes(0)
                        .build());

        registro.setSessoes(registro.getSessoes() + 1);
        registroRepository.save(registro);

        long ciclosConcluidos = registroRepository.somarSessoesPorUsuario(usuario.getId());
        return new SessaoEstudoResponse(hoje.toString(), registro.getSessoes(), ciclosConcluidos);
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
}
