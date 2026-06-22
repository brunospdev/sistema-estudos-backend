package com.planejamais.repository;

import com.planejamais.entity.Recorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, Long> {

    Optional<Recorrencia> findByIdAndUsuario_Id(Long id, Long usuarioId);
}
