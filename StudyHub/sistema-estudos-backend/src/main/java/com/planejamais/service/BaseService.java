package com.planejamais.service;

import com.planejamais.entity.Usuario;
import com.planejamais.exception.ResourceNotFoundException;
import com.planejamais.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseService {

    protected final UsuarioRepository usuarioRepository;

    protected Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
