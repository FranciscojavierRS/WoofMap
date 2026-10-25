package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Comentario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByLugarIdOrderByFechaDesc(Long lugarId, Pageable pageable);

    @Query("SELECT AVG(CAST(c.valoracion AS double)) FROM Comentario c WHERE c.lugar.id = :lugarId")
    Optional<Double> calcularMediaValoracion(@Param("lugarId") Long lugarId);

    long countByLugarId(Long lugarId);
}
