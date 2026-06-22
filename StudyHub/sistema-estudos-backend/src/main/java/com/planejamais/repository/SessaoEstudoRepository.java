package com.planejamais.repository;

import com.planejamais.entity.SessaoEstudo;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SessaoEstudoRepository extends JpaRepository<SessaoEstudo, Long> {

    @Query("SELECT COALESCE(SUM(s.duracaoMinutos), 0) FROM SessaoEstudo s WHERE s.usuario = :usuario AND s.inicioEm >= :inicio AND s.inicioEm < :fim")
    long somarMinutosPorPeriodo(@Param("usuario") Usuario usuario, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<SessaoEstudo> findByUsuarioAndInicioEmBetweenOrderByInicioEmDesc(Usuario usuario, LocalDateTime inicio, LocalDateTime fim);

    void deleteByUsuario_Id(Long usuarioId);
}
