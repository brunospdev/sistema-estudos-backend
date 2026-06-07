package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.*;
import com.planejamais.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    private DisciplinaResponse toResponse(Disciplina d) {
        List<DisciplinaResponse.TopicoResponse> topicos = assuntoRepository.findByDisciplina(d)
                .stream()
                .map(a -> new DisciplinaResponse.TopicoResponse(a.getId(), a.getTitulo(), a.isStatus()))
                .toList();
        return new DisciplinaResponse(d.getId(), d.getNome(), d.getDataCriacao(), topicos);
    }

    public List<DisciplinaResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        return disciplinaRepository.findByUsuario(usuario).stream()
                .map(this::toResponse)
                .toList();
    }

    public DisciplinaResponse criar(DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = Disciplina.builder()
                .nome(request.getNome())
                .usuario(usuario)
                .build();
        disciplinaRepository.save(disciplina);
        return toResponse(disciplina);
    }

    public DisciplinaResponse editar(Long id, DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        disciplina.setNome(request.getNome());
        disciplinaRepository.save(disciplina);
        return toResponse(disciplina);
    }

    public void excluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        assuntoRepository.deleteAll(assuntoRepository.findByDisciplina(disciplina));
        disciplinaRepository.delete(disciplina);
    }

    // Adicionar tópico diretamente na disciplina
    public DisciplinaResponse.TopicoResponse adicionarTopico(Long disciplinaId, String nome, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        Assunto assunto = Assunto.builder()
                .titulo(nome)
                .status(false)
                .disciplina(disciplina)
                .build();
        assuntoRepository.save(assunto);
        return new DisciplinaResponse.TopicoResponse(assunto.getId(), assunto.getTitulo(), assunto.isStatus());
    }

    // Toggle tópico
    public DisciplinaResponse.TopicoResponse toggleTopico(Long disciplinaId, Long topicoId, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(disciplinaId, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        Assunto assunto = assuntoRepository.findById(topicoId)
                .orElseThrow(() -> new RuntimeException("Tópico não encontrado."));
        if (!assunto.getDisciplina().getId().equals(disciplina.getId())) {
            throw new RuntimeException("Tópico não pertence a esta disciplina.");
        }
        assunto.setStatus(!assunto.isStatus());
        assuntoRepository.save(assunto);
        return new DisciplinaResponse.TopicoResponse(assunto.getId(), assunto.getTitulo(), assunto.isStatus());
    }
}
