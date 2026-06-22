package com.planejamais.repository;

import com.planejamais.domain.StatusEstudo;
import com.planejamais.entity.Assunto;
import com.planejamais.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {

    List<Assunto> findByDisciplinaOrderBySortOrderAscIdAsc(Disciplina disciplina);

    @Query("SELECT a FROM Assunto a WHERE a.disciplina.id IN :disciplinaIds ORDER BY a.sortOrder, a.id")
    List<Assunto> findByDisciplinaIdIn(@Param("disciplinaIds") List<Long> disciplinaIds);

    List<Assunto> findByDisciplinaIdInAndParentItemIsNullOrderBySortOrderAscIdAsc(List<Long> disciplinaIds);

    List<Assunto> findByParentItem_IdInOrderBySortOrderAscIdAsc(List<Long> parentItemIds);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.dataProgramada = :data AND a.statusEstudo <> :dominado ORDER BY a.sortOrder, a.id")
    List<Assunto> findProgramadosParaHoje(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data, @Param("dominado") StatusEstudo dominado);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.dataProgramada < :data AND a.statusEstudo <> :dominado ORDER BY a.dataProgramada, a.sortOrder, a.id")
    List<Assunto> findAtrasados(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data, @Param("dominado") StatusEstudo dominado);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.dataProgramada BETWEEN :de AND :ate ORDER BY a.dataProgramada, a.sortOrder, a.id")
    List<Assunto> findByUsuarioAndDataProgramadaBetween(@Param("usuarioId") Long usuarioId, @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.dataEntrega BETWEEN :de AND :ate ORDER BY a.dataEntrega, a.sortOrder, a.id")
    List<Assunto> findByUsuarioAndDataEntregaBetween(@Param("usuarioId") Long usuarioId, @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.dataEntrega = :data AND a.entregaConcluida = false ORDER BY a.sortOrder, a.id")
    List<Assunto> findEntregasParaHoje(@Param("usuarioId") Long usuarioId, @Param("data") LocalDate data);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.statusEstudo <> :dominado ORDER BY a.sortOrder, a.id")
    List<Assunto> findNaoDominadosByUsuario(@Param("usuarioId") Long usuarioId, @Param("dominado") StatusEstudo dominado);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.statusEstudo = :status ORDER BY a.ultimaSessaoEm ASC NULLS FIRST, a.sortOrder, a.id")
    List<Assunto> findByUsuarioAndStatusEstudoOrderByUltimaSessao(@Param("usuarioId") Long usuarioId, @Param("status") StatusEstudo status);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.statusEstudo = :status AND a.dataProgramada IS NOT NULL AND a.dataProgramada >= :hoje ORDER BY a.dataProgramada, a.sortOrder, a.id")
    List<Assunto> findNaoIniciadosComDataFutura(@Param("usuarioId") Long usuarioId, @Param("status") StatusEstudo status, @Param("hoje") LocalDate hoje);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.statusEstudo = :status ORDER BY a.sortOrder, a.id")
    List<Assunto> findByUsuarioAndStatusEstudo(@Param("usuarioId") Long usuarioId, @Param("status") StatusEstudo status);

    Optional<Assunto> findByIdAndDisciplina_Usuario_Id(Long id, Long usuarioId);

    int countByDisciplina(Disciplina disciplina);

    void deleteByDisciplina_IdIn(List<Long> disciplinaIds);

    @Query("SELECT a FROM Assunto a JOIN FETCH a.disciplina d WHERE d.usuario.id = :usuarioId AND a.tipo IN ('SIMULADO','PROVA','ATIVIDADE','ENTREGA','PRESENCIAL','OUTRO') ORDER BY a.entregaConcluida ASC, COALESCE(a.dataRealizada, a.dataEntrega, a.dataProgramada) DESC NULLS LAST, a.sortOrder, a.id")
    List<Assunto> findEventosByUsuario(@Param("usuarioId") Long usuarioId);

    List<Assunto> findByRecorrencia_IdOrderByIndiceOcorrenciaAsc(Long recorrenciaId);
}
