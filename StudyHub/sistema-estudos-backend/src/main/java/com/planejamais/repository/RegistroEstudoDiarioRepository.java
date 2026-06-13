package com.planejamais.repository;

import com.planejamais.entity.RegistroEstudoDiario;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroEstudoDiarioRepository extends JpaRepository<RegistroEstudoDiario, Long> {

    Optional<RegistroEstudoDiario> findByUsuarioAndData(Usuario usuario, LocalDate data);

    List<RegistroEstudoDiario> findByUsuario_IdAndDataGreaterThanEqual(Long usuarioId, LocalDate dataInicio);

    @Query("SELECT COALESCE(SUM(r.sessoes), 0) FROM RegistroEstudoDiario r WHERE r.usuario.id = :usuarioId")
    long somarSessoesPorUsuario(@Param("usuarioId") Long usuarioId);

    void deleteByUsuario_Id(Long usuarioId);
}
