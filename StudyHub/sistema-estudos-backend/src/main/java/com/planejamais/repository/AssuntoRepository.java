package com.planejamais.repository;

import com.planejamais.entity.Assunto;
import com.planejamais.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {

    List<Assunto> findByDisciplina(Disciplina disciplina);

    @Query("SELECT a FROM Assunto a WHERE a.disciplina.id IN :disciplinaIds ORDER BY a.id")
    List<Assunto> findByDisciplinaIdIn(@Param("disciplinaIds") List<Long> disciplinaIds);

    List<Assunto> findByDisciplina_Usuario_IdAndDataProgramada(Long usuarioId, LocalDate data);

    @Query("SELECT a FROM Assunto a WHERE a.disciplina.usuario.id = :usuarioId AND a.status = false AND a.dataProgramada < :data")
    List<Assunto> findPendentesAnteriores(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data);

    @Modifying
    @Query("UPDATE Assunto a SET a.dataProgramada = :novaData WHERE a.disciplina.usuario.id = :usuarioId AND a.status = false AND a.dataProgramada < :novaData")
    void moverPendentesParaHoje(@Param("usuarioId") Long usuarioId, @Param("novaData") LocalDate novaData);

    void deleteByDisciplina_IdIn(List<Long> disciplinaIds);
}
