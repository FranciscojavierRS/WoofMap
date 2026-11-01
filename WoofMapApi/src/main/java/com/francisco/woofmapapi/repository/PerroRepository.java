package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Perro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PerroRepository extends JpaRepository<Perro, Long> {

    @Query("""
            SELECT p,
                   COALESCE(SUM(CASE WHEN v.positivo = true THEN 1 ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN v.positivo = false THEN 1 ELSE 0 END), 0)
            FROM Perro p
            LEFT JOIN VotoPerro v ON v.perro = p AND v.fecha >= :desde
            GROUP BY p
            ORDER BY SUM(CASE WHEN v.positivo = true THEN 1 ELSE 0 END) DESC, p.id ASC
            """)
    List<Object[]> ranking(@Param("desde") Instant desde, Pageable pageable);

    @Query("""
            SELECT p FROM Perro p
            WHERE p.usuario.id <> :usuarioId
              AND NOT EXISTS (
                  SELECT v.id FROM VotoPerro v
                  WHERE v.perro = p AND v.usuario.id = :usuarioId
              )
            ORDER BY p.id ASC
            """)
    List<Perro> mazo(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
