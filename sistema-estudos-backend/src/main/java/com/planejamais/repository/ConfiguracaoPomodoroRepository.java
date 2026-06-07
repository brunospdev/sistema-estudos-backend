package com.planejamais.repository;

import com.planejamais.entity.ConfiguracaoPomodoro;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoPomodoroRepository extends JpaRepository<ConfiguracaoPomodoro, Long> {

    Optional<ConfiguracaoPomodoro> findByUsuario(Usuario usuario);

    void deleteByUsuario_Id(Long usuarioId);
}
