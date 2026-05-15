package com.planejamais.controller;

import com.planejamais.dto.PerfilRequest;
import com.planejamais.dto.PerfilResponse;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> getPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return ResponseEntity.ok(new PerfilResponse(usuario.getId(), usuario.getNome(), usuario.getEmail()));
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilResponse> atualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerfilRequest request
    ) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (request.getNome() != null && !request.getNome().isBlank()) {
            usuario.setNome(request.getNome().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            usuario.setEmail(request.getEmail().trim());
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new PerfilResponse(usuario.getId(), usuario.getNome(), usuario.getEmail()));
    }

    @DeleteMapping("/perfil")
    public ResponseEntity<Void> excluirConta(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }
}
