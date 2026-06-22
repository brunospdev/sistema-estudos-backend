package com.planejamais.service;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.exception.ForbiddenException;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssuntoService extends BaseService {

    private final AssuntoRepository assuntoRepository;
    private final DisciplinaRepository disciplinaRepository;

    public AssuntoService(UsuarioRepository usuarioRepository,
                          AssuntoRepository assuntoRepository,
                          DisciplinaRepository disciplinaRepository) {
        super(usuarioRepository);
        this.assuntoRepository = assuntoRepository;
        this.disciplinaRepository = disciplinaRepository;
    }

    private AssuntoResponse toResponse(Assunto a) {
        return new AssuntoResponse(
                a.getId(),
                a.getTitulo(),
                a.getStatusEstudo(),
                a.getDataProgramada(),
                a.getDataConclusao(),
                a.getDisciplina().getId(),
                a.getDisciplina().getNome()
        );
    }

    public List<AssuntoResponse> listarPorDisciplina(Long disciplinaId, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        return assuntoRepository.findByDisciplinaOrderBySortOrderAscIdAsc(disciplina).stream()
                .map(this::toResponse)
                .toList();
    }

    public AssuntoResponse criar(AssuntoRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(request.getDisciplinaId(), usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));

        int maxOrder = assuntoRepository.findByDisciplinaOrderBySortOrderAscIdAsc(disciplina).stream()
                .mapToInt(Assunto::getSortOrder)
                .max()
                .orElse(-1);

        Assunto assunto = Assunto.builder()
                .titulo(request.getTitulo())
                .statusEstudo(StatusEstudo.NAO_INICIADO)
                .dataProgramada(request.getDataProgramada())
                .sortOrder(maxOrder + 1)
                .disciplina(disciplina)
                .build();

        assuntoRepository.save(assunto);
        return toResponse(assunto);
    }

    public AssuntoResponse concluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Assunto assunto = assuntoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assunto não encontrado."));

        if (!assunto.getDisciplina().getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Acesso negado.");
        }

        assunto.setStatusEstudo(StatusEstudo.DOMINADO);
        assunto.setDataConclusao(LocalDate.now());
        assuntoRepository.save(assunto);
        return toResponse(assunto);
    }
}
