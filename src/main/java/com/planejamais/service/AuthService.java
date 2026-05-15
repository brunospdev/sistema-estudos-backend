package com.planejamais.service;

import com.planejamais.dto.*;
import com.planejamais.entity.Usuario;
import com.planejamais.repository.UsuarioRepository;
import com.planejamais.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .build();

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getNome(), usuario.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas."));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Credenciais inválidas.");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getNome(), usuario.getEmail());
    }
}
