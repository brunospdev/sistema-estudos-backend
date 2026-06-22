package com.planejamais.repository;

import com.planejamais.entity.MarcoUsuario;
import com.planejamais.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarcoUsuarioRepository extends JpaRepository<MarcoUsuario, Long> {

    List<MarcoUsuario> findByUsuarioOrderByDataAsc(Usuario usuario);

    @Query("SELECT m FROM MarcoUsuario m WHERE m.usuario = :usuario AND m.data >= :hoje ORDER BY m.data ASC")
    List<MarcoUsuario> findProximos(@Param("usuario") Usuario usuario, @Param("hoje") LocalDate hoje);

    Optional<MarcoUsuario> findByIdAndUsuario(Long id, Usuario usuario);

    Optional<MarcoUsuario> findByUsuarioAndEhPrincipalTrue(Usuario usuario);

    @Modifying
    @Query("UPDATE MarcoUsuario m SET m.ehPrincipal = false WHERE m.usuario = :usuario AND m.ehPrincipal = true")
    void desmarcarPrincipal(@Param("usuario") Usuario usuario);

    @Query("SELECT m FROM MarcoUsuario m WHERE m.usuario.id = :usuarioId AND m.data BETWEEN :de AND :ate ORDER BY m.data")
    List<MarcoUsuario> findByUsuarioAndDataBetween(@Param("usuarioId") Long usuarioId, @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    void deleteByUsuario_Id(Long usuarioId);
}
