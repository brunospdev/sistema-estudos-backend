package com.planejamais.service;

import com.planejamais.dto.PerfilRequest;
import com.planejamais.dto.PerfilResponse;
import com.planejamais.entity.Usuario;
import com.planejamais.exception.ConflictException;
import com.planejamais.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UsuarioService extends BaseService {

    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final RegistroEstudoDiarioRepository registroRepository;
    private final ConfiguracaoPomodoroRepository configRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final MarcoUsuarioRepository marcoRepository;
    private final SessaoEstudoRepository sessaoRepository;
    private final EventoStatusRepository eventoStatusRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          DisciplinaRepository disciplinaRepository,
                          AssuntoRepository assuntoRepository,
                          RegistroEstudoDiarioRepository registroRepository,
                          ConfiguracaoPomodoroRepository configRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          PreferenciasUsuarioRepository preferenciasRepository,
                          MarcoUsuarioRepository marcoRepository,
                          SessaoEstudoRepository sessaoRepository,
                          EventoStatusRepository eventoStatusRepository) {
        super(usuarioRepository);
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.registroRepository = registroRepository;
        this.configRepository = configRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.preferenciasRepository = preferenciasRepository;
        this.marcoRepository = marcoRepository;
        this.sessaoRepository = sessaoRepository;
        this.eventoStatusRepository = eventoStatusRepository;
    }

    public PerfilResponse obterPerfil(String email) {
        Usuario usuario = getUsuario(email);
        return toResponse(usuario);
    }

    @Transactional
    public PerfilResponse atualizarPerfil(String email, PerfilRequest request) {
        Usuario usuario = getUsuario(email);

        if (request.getNome() != null && !request.getNome().isBlank()) {
            usuario.setNome(request.getNome().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String novoEmail = request.getEmail().trim();
            if (usuarioRepository.existsByEmailAndIdNot(novoEmail, usuario.getId())) {
                throw new ConflictException("E-mail já está em uso por outra conta.");
            }
            usuario.setEmail(novoEmail);
        }

        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional
    public void excluirConta(String email) {
        Usuario usuario = getUsuario(email);
        Long usuarioId = usuario.getId();

        var disciplinas = disciplinaRepository.findByUsuario(usuario);
        var disciplinaIds = disciplinas.stream().map(d -> d.getId()).toList();

        if (!disciplinaIds.isEmpty()) {
            assuntoRepository.deleteByDisciplina_IdIn(disciplinaIds);
            disciplinaRepository.deleteByUsuario_Id(usuarioId);
        }

        eventoStatusRepository.deleteByUsuario_Id(usuarioId);
        sessaoRepository.deleteByUsuario_Id(usuarioId);
        marcoRepository.deleteByUsuario_Id(usuarioId);
        preferenciasRepository.deleteByUsuario_Id(usuarioId);
        registroRepository.deleteByUsuario_Id(usuarioId);
        configRepository.deleteByUsuario_Id(usuarioId);
        refreshTokenRepository.revokeAllByUsuarioId(usuarioId);
        passwordResetTokenRepository.deleteByUsuario_Id(usuarioId);

        usuarioRepository.delete(usuario);
        log.info("Conta excluída: {}", email);
    }

    private PerfilResponse toResponse(Usuario usuario) {
        return new PerfilResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
