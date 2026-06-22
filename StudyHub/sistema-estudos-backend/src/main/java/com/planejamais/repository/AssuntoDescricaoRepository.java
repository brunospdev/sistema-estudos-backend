package com.planejamais.repository;

import com.planejamais.entity.AssuntoDescricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssuntoDescricaoRepository extends JpaRepository<AssuntoDescricao, Long> {

    List<AssuntoDescricao> findByAssunto_IdInOrderBySortOrderAscIdAsc(List<Long> assuntoIds);

    List<AssuntoDescricao> findByAssunto_IdOrderBySortOrderAscIdAsc(Long assuntoId);
}
