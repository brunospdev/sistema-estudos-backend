package com.planejamais.controller;

import com.planejamais.dto.PerfilRequest;
import com.planejamais.dto.PerfilResponse;
import com.planejamais.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.obterPerfil(userDetails.getUsername()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilResponse> atualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PerfilRequest request
    ) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(userDetails.getUsername(), request));
    }

    @DeleteMapping("/perfil")
    public ResponseEntity<Void> excluirConta(@AuthenticationPrincipal UserDetails userDetails) {
        usuarioService.excluirConta(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
