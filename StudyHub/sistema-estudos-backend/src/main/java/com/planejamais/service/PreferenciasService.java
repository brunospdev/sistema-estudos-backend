package com.planejamais.service;

import com.planejamais.domain.LayoutMode;
import com.planejamais.dto.MarcoResumoResponse;
import com.planejamais.dto.PreferenciasRequest;
import com.planejamais.dto.PreferenciasResponse;
import com.planejamais.entity.Disciplina;
import com.planejamais.entity.MarcoUsuario;
import com.planejamais.entity.PreferenciasUsuario;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.DisciplinaRepository;
import com.planejamais.repository.PreferenciasUsuarioRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PreferenciasService extends BaseService {

    public static final String PLANO_OCULTA_NOME = "Meu plano";

    private final PreferenciasUsuarioRepository preferenciasRepository;
    private final DisciplinaRepository disciplinaRepository;

    public PreferenciasService(UsuarioRepository usuarioRepository,
                               PreferenciasUsuarioRepository preferenciasRepository,
                               DisciplinaRepository disciplinaRepository) {
        super(usuarioRepository);
        this.preferenciasRepository = preferenciasRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    @Transactional
    public PreferenciasResponse getOrCreate(String email) {
        Usuario usuario = getUsuario(email);
        PreferenciasUsuario prefs = preferenciasRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPreferenciasPadrao(usuario));
        if (prefs.getLayoutMode() == LayoutMode.LISTA) {
            ensurePlanoOculto(usuario);
        }
        return toResponse(prefs);
    }

    @Transactional
    public PreferenciasResponse update(String email, PreferenciasRequest request) {
        Usuario usuario = getUsuario(email);
        PreferenciasUsuario prefs = preferenciasRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPreferenciasPadrao(usuario));

        prefs.setLayoutMode(request.getLayoutMode());
        prefs.setLabelGrupo(request.getLabelGrupo());
        prefs.setLabelItem(request.getLabelItem());
        prefs.setMetaHorasDiarias(request.getMetaHorasDiarias());
        if (request.getProfundidadeGrupos() != null) {
            prefs.setProfundidadeGrupos(request.getProfundidadeGrupos());
        }
        if (request.getLabelGrupoNivel2() != null) {
            prefs.setLabelGrupoNivel2(request.getLabelGrupoNivel2().isBlank() ? null : request.getLabelGrupoNivel2());
        }
        if (request.getPresetPersona() != null) {
            prefs.setPresetPersona(request.getPresetPersona().isBlank() ? null : request.getPresetPersona());
        }
        preferenciasRepository.save(prefs);

        if (request.getLayoutMode() == LayoutMode.LISTA) {
            ensurePlanoOculto(usuario);
        }

        return toResponse(prefs);
    }

    @Transactional
    public Disciplina ensurePlanoOculto(Usuario usuario) {
        return disciplinaRepository.findByUsuarioAndOcultaTrue(usuario)
                .orElseGet(() -> {
                    int maxOrder = disciplinaRepository.findByUsuarioWithUsuario(usuario).stream()
                            .mapToInt(Disciplina::getSortOrder)
                            .max()
                            .orElse(-1);
                    Disciplina plano = Disciplina.builder()
                            .nome(PLANO_OCULTA_NOME)
                            .usuario(usuario)
                            .oculta(true)
                            .sortOrder(maxOrder + 1)
                            .build();
                    return disciplinaRepository.save(plano);
                });
    }

    @Transactional
    public PreferenciasUsuario criarPreferenciasPadrao(Usuario usuario) {
        PreferenciasUsuario prefs = PreferenciasUsuario.builder()
                .usuario(usuario)
                .build();
        return preferenciasRepository.save(prefs);
    }

    public PreferenciasUsuario getPreferencias(Usuario usuario) {
        return preferenciasRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPreferenciasPadrao(usuario));
    }

    private PreferenciasResponse toResponse(PreferenciasUsuario prefs) {
        MarcoResumoResponse marcoPrincipal = null;
        if (prefs.getMarcoPrincipal() != null) {
            marcoPrincipal = toMarcoResumo(prefs.getMarcoPrincipal());
        }
        return new PreferenciasResponse(
                prefs.getLayoutMode(),
                prefs.getLabelGrupo(),
                prefs.getLabelItem(),
                prefs.getMetaHorasDiarias(),
                prefs.getProfundidadeGrupos(),
                prefs.getLabelGrupoNivel2(),
                prefs.getPresetPersona(),
                marcoPrincipal
        );
    }

    static MarcoResumoResponse toMarcoResumo(MarcoUsuario marco) {
        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), marco.getData());
        return new MarcoResumoResponse(marco.getId(), marco.getTitulo(), marco.getData(), diasRestantes);
    }
}
