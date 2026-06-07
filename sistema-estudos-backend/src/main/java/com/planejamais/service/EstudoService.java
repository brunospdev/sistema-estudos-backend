package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.ConfiguracaoPomodoro;
import com.planejamais.entity.RegistroEstudoDiario;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.ConfiguracaoPomodoroRepository;
import com.planejamais.repository.RegistroEstudoDiarioRepository;
import com.planejamais.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EstudoService {

    private static final int HEATMAP_DIAS = 84;

    private final RegistroEstudoDiarioRepository registroRepository;
    private final ConfiguracaoPomodoroRepository configRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
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

    @Transactional
    public PomodoroResponse sincronizarHeatmap(String email, HeatmapSyncRequest request) {
        if (request.getDias() == null || request.getDias().isEmpty()) {
            return obterPomodoro(email);
        }

        Usuario usuario = getUsuario(email);

        request.getDias().forEach((dataStr, quantidade) -> {
            if (quantidade == null || quantidade <= 0) return;

            LocalDate data;
            try {
                data = LocalDate.parse(dataStr);
            } catch (Exception ex) {
                return;
            }

            RegistroEstudoDiario registro = registroRepository.findByUsuarioAndData(usuario, data)
                    .orElseGet(() -> RegistroEstudoDiario.builder()
                            .usuario(usuario)
                            .data(data)
                            .sessoes(0)
                            .build());

            registro.setSessoes(Math.max(registro.getSessoes(), quantidade));
            registroRepository.save(registro);
        });

        return obterPomodoro(email);
    }

    @Transactional
    public void excluirDadosDoUsuario(Long usuarioId) {
        registroRepository.deleteByUsuario_Id(usuarioId);
        configRepository.deleteByUsuario_Id(usuarioId);
    }
}
