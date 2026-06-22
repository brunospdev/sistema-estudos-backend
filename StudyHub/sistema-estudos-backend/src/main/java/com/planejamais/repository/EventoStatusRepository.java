package com.planejamais.repository;

import com.planejamais.entity.EventoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoStatusRepository extends JpaRepository<EventoStatus, Long> {

    @Modifying
    @Query("DELETE FROM EventoStatus e WHERE e.usuario.id = :usuarioId")
    void deleteByUsuario_Id(@Param("usuarioId") Long usuarioId);
}
