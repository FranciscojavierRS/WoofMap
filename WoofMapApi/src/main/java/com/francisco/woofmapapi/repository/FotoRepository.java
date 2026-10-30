package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Foto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByLugarIdOrderByFechaSubidaDesc(Long lugarId, Pageable pageable);

    List<Foto> findByLugarId(Long lugarId);

    long countByLugarId(Long lugarId);
}
