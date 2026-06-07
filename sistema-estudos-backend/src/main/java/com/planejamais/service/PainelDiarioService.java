package com.planejamais.service;

import com.planejamais.dto.AssuntoResponse;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.AssuntoRepository;
import com.planejamais.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PainelDiarioService {

    private final AssuntoRepository assuntoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public List<AssuntoResponse> getPainelDiario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        LocalDate hoje = LocalDate.now();

        // Move todos os pendentes de dias anteriores para hoje
        assuntoRepository.moverPendentesParaHoje(usuario.getId(), hoje);

        // Retorna os assuntos programados para hoje
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
