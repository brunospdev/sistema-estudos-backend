package com.planejamais.controller;

import com.planejamais.dto.MarcoRequest;
import com.planejamais.dto.MarcoResponse;
import com.planejamais.service.MarcoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcos")
@RequiredArgsConstructor
public class MarcoController {

    private final MarcoService marcoService;

    @GetMapping
    public ResponseEntity<List<MarcoResponse>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(marcoService.listar(user.getUsername()));
    }

    @GetMapping("/proximos")
    public ResponseEntity<List<MarcoResponse>> listarProximos(
            @RequestParam(defaultValue = "5") int limite,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(marcoService.listarProximos(user.getUsername(), limite));
    }

    @PostMapping
    public ResponseEntity<MarcoResponse> criar(
            @Valid @RequestBody MarcoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(marcoService.criar(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MarcoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(marcoService.atualizar(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        marcoService.excluir(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
