package com.planejamais.controller;

import com.planejamais.dto.*;
import com.planejamais.service.AssuntoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assuntos")
@RequiredArgsConstructor
public class AssuntoController {

    private final AssuntoService assuntoService;

    @GetMapping("/{disciplinaId}")
    public ResponseEntity<List<AssuntoResponse>> listarPorDisciplina(@PathVariable Long disciplinaId,
                                                                      @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(assuntoService.listarPorDisciplina(disciplinaId, user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<AssuntoResponse> criar(@Valid @RequestBody AssuntoRequest request,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(assuntoService.criar(request, user.getUsername()));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<AssuntoResponse> concluir(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(assuntoService.concluir(id, user.getUsername()));
    }
}
