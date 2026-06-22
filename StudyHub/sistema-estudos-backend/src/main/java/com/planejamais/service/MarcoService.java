package com.planejamais.service;

import com.planejamais.dto.MarcoRequest;
import com.planejamais.dto.MarcoResponse;
import com.planejamais.entity.Disciplina;
import com.planejamais.entity.MarcoUsuario;
import com.planejamais.entity.PreferenciasUsuario;
import com.planejamais.entity.Usuario;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.DisciplinaRepository;
import com.planejamais.repository.MarcoUsuarioRepository;
import com.planejamais.repository.PreferenciasUsuarioRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcoService extends BaseService {

    private final MarcoUsuarioRepository marcoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PreferenciasUsuarioRepository preferenciasRepository;

    public MarcoService(UsuarioRepository usuarioRepository,
                        MarcoUsuarioRepository marcoRepository,
                        DisciplinaRepository disciplinaRepository,
                        PreferenciasUsuarioRepository preferenciasRepository) {
        super(usuarioRepository);
        this.marcoRepository = marcoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.preferenciasRepository = preferenciasRepository;
    }

    public List<MarcoResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        return marcoRepository.findByUsuarioOrderByDataAsc(usuario).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MarcoResponse> listarProximos(String email, int limite) {
        Usuario usuario = getUsuario(email);
        return marcoRepository.findProximos(usuario, java.time.LocalDate.now()).stream()
                .limit(limite)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MarcoResponse criar(MarcoRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = resolverDisciplina(request.getDisciplinaId(), usuario);

        MarcoUsuario marco = MarcoUsuario.builder()
                .usuario(usuario)
                .disciplina(disciplina)
                .tipo(request.getTipo())
                .titulo(request.getTitulo())
                .data(request.getData())
                .notas(request.getNotas())
                .build();

        marcoRepository.save(marco);

        if (Boolean.TRUE.equals(request.getEhPrincipal())) {
            marcarComoPrincipal(marco, usuario);
        }

        return toResponse(marco);
    }

    @Transactional
    public MarcoResponse atualizar(Long id, MarcoRequest request, String email) {
        Usuario usuario = getUsuario(email);
        MarcoUsuario marco = marcoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Marco não encontrado."));

        marco.setTipo(request.getTipo());
        marco.setTitulo(request.getTitulo());
        marco.setData(request.getData());
        marco.setNotas(request.getNotas());
        marco.setDisciplina(resolverDisciplina(request.getDisciplinaId(), usuario));

        marcoRepository.save(marco);

        if (Boolean.TRUE.equals(request.getEhPrincipal())) {
            marcarComoPrincipal(marco, usuario);
        } else if (Boolean.FALSE.equals(request.getEhPrincipal()) && marco.isEhPrincipal()) {
            desmarcarPrincipal(marco, usuario);
        }

        return toResponse(marco);
    }

    @Transactional
    public void excluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        MarcoUsuario marco = marcoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Marco não encontrado."));

        if (marco.isEhPrincipal()) {
            preferenciasRepository.findByUsuario(usuario).ifPresent(prefs -> {
                prefs.setMarcoPrincipal(null);
                preferenciasRepository.save(prefs);
            });
            usuario.setTargetExamDate(null);
            usuarioRepository.save(usuario);
        }

        marcoRepository.delete(marco);
    }

    private void marcarComoPrincipal(MarcoUsuario marco, Usuario usuario) {
        marcoRepository.desmarcarPrincipal(usuario);
        marco.setEhPrincipal(true);
        marcoRepository.save(marco);

        usuario.setTargetExamDate(marco.getData());
        usuarioRepository.save(usuario);

        PreferenciasUsuario prefs = preferenciasRepository.findByUsuario(usuario)
                .orElseGet(() -> PreferenciasUsuario.builder().usuario(usuario).build());
        prefs.setMarcoPrincipal(marco);
        preferenciasRepository.save(prefs);
    }

    private void desmarcarPrincipal(MarcoUsuario marco, Usuario usuario) {
        marco.setEhPrincipal(false);
        marcoRepository.save(marco);

        usuario.setTargetExamDate(null);
        usuarioRepository.save(usuario);

        preferenciasRepository.findByUsuario(usuario).ifPresent(prefs -> {
            if (prefs.getMarcoPrincipal() != null && prefs.getMarcoPrincipal().getId().equals(marco.getId())) {
                prefs.setMarcoPrincipal(null);
                preferenciasRepository.save(prefs);
            }
        });
    }

    private Disciplina resolverDisciplina(Long disciplinaId, Usuario usuario) {
        if (disciplinaId == null) {
            return null;
        }
        return disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
    }

    private MarcoResponse toResponse(MarcoUsuario marco) {
        Long disciplinaId = marco.getDisciplina() != null ? marco.getDisciplina().getId() : null;
        String disciplinaNome = marco.getDisciplina() != null ? marco.getDisciplina().getNome() : null;
        return new MarcoResponse(
                marco.getId(),
                marco.getTipo(),
                marco.getTitulo(),
                marco.getData(),
                marco.getNotas(),
                disciplinaId,
                disciplinaNome,
                marco.isEhPrincipal(),
                marco.getCreatedAt()
        );
    }
}
