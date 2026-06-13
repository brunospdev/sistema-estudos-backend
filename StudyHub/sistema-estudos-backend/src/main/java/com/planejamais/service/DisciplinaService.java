package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.exception.ForbiddenException;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DisciplinaService extends BaseService {

    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;

    public DisciplinaService(UsuarioRepository usuarioRepository,
                             DisciplinaRepository disciplinaRepository,
                             AssuntoRepository assuntoRepository) {
        super(usuarioRepository);
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
    }

    public List<DisciplinaResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        List<Disciplina> disciplinas = disciplinaRepository.findByUsuarioWithUsuario(usuario);

        if (disciplinas.isEmpty()) {
            return List.of();
        }

        List<Long> disciplinaIds = disciplinas.stream().map(Disciplina::getId).toList();
        Map<Long, List<Assunto>> assuntosPorDisciplina = assuntoRepository.findByDisciplinaIdIn(disciplinaIds)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getDisciplina().getId()));

        return disciplinas.stream()
                .map(d -> toResponse(d, assuntosPorDisciplina.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    public DisciplinaResponse criar(DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = Disciplina.builder()
                .nome(request.getNome())
                .usuario(usuario)
                .build();
        disciplinaRepository.save(disciplina);
        return toResponse(disciplina, List.of());
    }

    public DisciplinaResponse editar(Long id, DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        disciplina.setNome(request.getNome());
        disciplinaRepository.save(disciplina);
        List<Assunto> assuntos = assuntoRepository.findByDisciplina(disciplina);
        return toResponse(disciplina, assuntos);
    }

    @Transactional
    public void excluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        assuntoRepository.deleteAll(assuntoRepository.findByDisciplina(disciplina));
        disciplinaRepository.delete(disciplina);
    }

    public DisciplinaResponse.TopicoResponse adicionarTopico(Long disciplinaId, String nome, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        Assunto assunto = Assunto.builder()
                .titulo(nome)
                .status(false)
                .disciplina(disciplina)
                .build();
        assuntoRepository.save(assunto);
        return new DisciplinaResponse.TopicoResponse(assunto.getId(), assunto.getTitulo(), assunto.isStatus());
    }

    public DisciplinaResponse.TopicoResponse toggleTopico(Long disciplinaId, Long topicoId, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada."));
        Assunto assunto = assuntoRepository.findById(topicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tópico não encontrado."));
        if (!assunto.getDisciplina().getId().equals(disciplina.getId())) {
            throw new ForbiddenException("Tópico não pertence a esta disciplina.");
        }
        assunto.setStatus(!assunto.isStatus());
        assuntoRepository.save(assunto);
        return new DisciplinaResponse.TopicoResponse(assunto.getId(), assunto.getTitulo(), assunto.isStatus());
    }

    private DisciplinaResponse toResponse(Disciplina d, List<Assunto> assuntos) {
        List<DisciplinaResponse.TopicoResponse> topicos = assuntos.stream()
                .map(a -> new DisciplinaResponse.TopicoResponse(a.getId(), a.getTitulo(), a.isStatus()))
                .toList();
        return new DisciplinaResponse(d.getId(), d.getNome(), d.getDataCriacao(), topicos);
    }
}
