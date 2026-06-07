package com.planejamais.repository;

import com.planejamais.entity.Disciplina;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    List<Disciplina> findByUsuario(Usuario usuario);
    Optional<Disciplina> findByIdAndUsuario(Long id, Usuario usuario);
}
