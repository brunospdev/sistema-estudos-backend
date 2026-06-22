package com.planejamais.repository;

import com.planejamais.entity.PreferenciasUsuario;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciasUsuarioRepository extends JpaRepository<PreferenciasUsuario, Long> {

    Optional<PreferenciasUsuario> findByUsuario(Usuario usuario);

    void deleteByUsuario_Id(Long usuarioId);
}
