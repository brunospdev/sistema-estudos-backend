package com.planejamais.service;

import com.planejamais.dto.AssuntoResponse;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PainelDiarioService extends BaseService {

    private final AssuntoRepository assuntoRepository;

    public PainelDiarioService(UsuarioRepository usuarioRepository,
                               AssuntoRepository assuntoRepository) {
        super(usuarioRepository);
        this.assuntoRepository = assuntoRepository;
    }

    @Transactional
    public List<AssuntoResponse> getPainelDiario(String email) {
        Usuario usuario = getUsuario(email);
        LocalDate hoje = LocalDate.now();

        assuntoRepository.moverPendentesParaHoje(usuario.getId(), hoje);

        return assuntoRepository
                .findByDisciplina_Usuario_IdAndDataProgramada(usuario.getId(), hoje)
                .stream()
                .map(a -> new AssuntoResponse(
                        a.getId(),
                        a.getTitulo(),
                        a.isStatus(),
                        a.getDataProgramada(),
                        a.getDataConclusao(),
                        a.getDisciplina().getId(),
                        a.getDisciplina().getNome()
                ))
                .toList();
    }
}
