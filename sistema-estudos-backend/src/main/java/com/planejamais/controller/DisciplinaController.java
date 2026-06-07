package com.planejamais.controller;

import com.planejamais.dto.*;
import com.planejamais.service.DisciplinaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @GetMapping
    public ResponseEntity<List<DisciplinaResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.listar(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<DisciplinaResponse> criar(@Valid @RequestBody DisciplinaRequest request,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.criar(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaResponse> editar(@PathVariable Long id,
                                                      @Valid @RequestBody DisciplinaRequest request,
                                                      @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.editar(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails user) {
        disciplinaService.excluir(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    // Endpoints de tópicos dentro de disciplina
    @PostMapping("/{id}/topicos")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> adicionarTopico(
            @PathVariable Long id,
            @Valid @RequestBody TopicoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.adicionarTopico(id, request.getNome(), user.getUsername()));
    }

    @PatchMapping("/{disciplinaId}/topicos/{topicoId}/toggle")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> toggleTopico(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.toggleTopico(disciplinaId, topicoId, user.getUsername()));
    }

    @Data
    static class TopicoRequest {
        @NotBlank(message = "Nome do tópico é obrigatório.")
        private String nome;
    }
}
