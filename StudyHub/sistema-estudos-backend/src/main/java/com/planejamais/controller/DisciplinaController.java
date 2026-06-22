package com.planejamais.controller;

import com.planejamais.dto.*;
import com.planejamais.service.DisciplinaService;
import jakarta.validation.Valid;
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

    @PutMapping("/reorder")
    public ResponseEntity<Void> reordenar(@Valid @RequestBody ReorderRequest request,
                                            @AuthenticationPrincipal UserDetails user) {
        disciplinaService.reordenar(request, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/topicos")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> adicionarTopico(
            @PathVariable Long id,
            @Valid @RequestBody TopicoCreateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.adicionarTopico(id, request, user.getUsername()));
    }

    @PutMapping("/{disciplinaId}/topicos/{topicoId}")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> editarTopico(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @Valid @RequestBody TopicoUpdateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.editarTopico(disciplinaId, topicoId, request, user.getUsername()));
    }

    @PatchMapping("/{disciplinaId}/topicos/{topicoId}/status")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> patchStatus(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @Valid @RequestBody TopicoStatusRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.patchStatus(disciplinaId, topicoId, request, user.getUsername()));
    }

    @PatchMapping("/{disciplinaId}/topicos/{topicoId}/agenda")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> patchAgenda(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @Valid @RequestBody TopicoAgendaRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.patchAgenda(disciplinaId, topicoId, request, user.getUsername()));
    }

    @DeleteMapping("/{disciplinaId}/topicos/{topicoId}")
    public ResponseEntity<Void> excluirTopico(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @AuthenticationPrincipal UserDetails user) {
        disciplinaService.excluirTopico(disciplinaId, topicoId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/topicos/reorder")
    public ResponseEntity<Void> reordenarTopicos(
            @PathVariable Long id,
            @Valid @RequestBody ReorderRequest request,
            @AuthenticationPrincipal UserDetails user) {
        disciplinaService.reordenarTopicos(id, request, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{disciplinaId}/topicos/{topicoId}/avaliacao")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> patchAvaliacao(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @Valid @RequestBody TopicoAvaliacaoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.patchAvaliacao(disciplinaId, topicoId, request, user.getUsername()));
    }

    @PatchMapping("/{disciplinaId}/topicos/{topicoId}/toggle")
    public ResponseEntity<DisciplinaResponse.TopicoResponse> toggleTopico(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.toggleTopico(disciplinaId, topicoId, user.getUsername()));
    }

    @PostMapping("/{disciplinaId}/topicos/{topicoId}/descricoes")
    public ResponseEntity<DescricaoResponse> adicionarDescricao(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @Valid @RequestBody DescricaoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.adicionarDescricao(disciplinaId, topicoId, request, user.getUsername()));
    }

    @PutMapping("/{disciplinaId}/topicos/{topicoId}/descricoes/{descricaoId}")
    public ResponseEntity<DescricaoResponse> editarDescricao(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @PathVariable Long descricaoId,
            @Valid @RequestBody DescricaoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(disciplinaService.editarDescricao(disciplinaId, topicoId, descricaoId, request, user.getUsername()));
    }

    @DeleteMapping("/{disciplinaId}/topicos/{topicoId}/descricoes/{descricaoId}")
    public ResponseEntity<Void> excluirDescricao(
            @PathVariable Long disciplinaId,
            @PathVariable Long topicoId,
            @PathVariable Long descricaoId,
            @AuthenticationPrincipal UserDetails user) {
        disciplinaService.excluirDescricao(disciplinaId, topicoId, descricaoId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
