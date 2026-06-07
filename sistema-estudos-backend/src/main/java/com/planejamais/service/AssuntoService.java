package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssuntoService {

    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private AssuntoResponse toResponse(Assunto a) {
        return new AssuntoResponse(
                a.getId(),
                a.getTitulo(),
                a.isStatus(),
                a.getDataProgramada(),
                a.getDataConclusao(),
                a.getDisciplina().getId(),
                a.getDisciplina().getNome()
        );
    }

    public List<AssuntoResponse> listarPorDisciplina(Long disciplinaId, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        return assuntoRepository.findByDisciplina(disciplina).stream()
                .map(this::toResponse)
                .toList();
    }

    public AssuntoResponse criar(AssuntoRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(request.getDisciplinaId(), usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));

        Assunto assunto = Assunto.builder()
                .titulo(request.getTitulo())
                .status(false)
                .dataProgramada(request.getDataProgramada())
                .disciplina(disciplina)
                .build();

        assuntoRepository.save(assunto);
        return toResponse(assunto);
    }

    public AssuntoResponse concluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Assunto assunto = assuntoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assunto não encontrado."));

        if (!assunto.getDisciplina().getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Acesso negado.");
        }

        assunto.setStatus(true);
        assunto.setDataConclusao(LocalDate.now());
        assuntoRepository.save(assunto);
        return toResponse(assunto);
    }
}
