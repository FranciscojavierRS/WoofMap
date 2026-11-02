package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    Optional<Favorito> findByUsuarioIdAndLugarId(Long usuarioId, Long lugarId);

    boolean existsByUsuarioIdAndLugarId(Long usuarioId, Long lugarId);
}
