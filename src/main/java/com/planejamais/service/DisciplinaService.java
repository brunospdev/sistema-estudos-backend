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
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    public List<DisciplinaResponse> listar(String email) {
        Usuario usuario = getUsuario(email);
        return disciplinaRepository.findByUsuario(usuario).stream()
                .map(d -> new DisciplinaResponse(d.getId(), d.getNome(), d.getDataCriacao()))
                .toList();
    }

    public DisciplinaResponse criar(DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = Disciplina.builder()
                .nome(request.getNome())
                .usuario(usuario)
                .build();
        disciplinaRepository.save(disciplina);
        return new DisciplinaResponse(disciplina.getId(), disciplina.getNome(), disciplina.getDataCriacao());
    }

    public DisciplinaResponse editar(Long id, DisciplinaRequest request, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        disciplina.setNome(request.getNome());
        disciplinaRepository.save(disciplina);
        return new DisciplinaResponse(disciplina.getId(), disciplina.getNome(), disciplina.getDataCriacao());
    }

    public void excluir(Long id, String email) {
        Usuario usuario = getUsuario(email);
        Disciplina disciplina = disciplinaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));
        disciplinaRepository.delete(disciplina);
    }
}
