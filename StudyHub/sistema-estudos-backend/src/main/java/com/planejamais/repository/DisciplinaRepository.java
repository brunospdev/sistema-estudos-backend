package com.planejamais.repository;

import com.planejamais.entity.Disciplina;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    List<Disciplina> findByUsuario(Usuario usuario);

    Optional<Disciplina> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("SELECT DISTINCT d FROM Disciplina d LEFT JOIN FETCH d.usuario WHERE d.usuario = :usuario ORDER BY d.sortOrder, d.dataCriacao")
    List<Disciplina> findByUsuarioWithUsuario(Usuario usuario);

    Optional<Disciplina> findByUsuarioAndOcultaTrue(Usuario usuario);

    Optional<Disciplina> findByUsuarioAndNome(Usuario usuario, String nome);

    void deleteByUsuario_Id(Long usuarioId);
}
