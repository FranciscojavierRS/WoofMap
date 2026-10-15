package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {

    List<Lugar> findByCategoria(String categoria);

    @Query(value = """
            SELECT l.* FROM lugar l
            WHERE ST_DWithin(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radio)
            ORDER BY ST_Distance(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) ASC
            """, nativeQuery = true)
    List<Lugar> findNearby(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radio") double radio
    );

    @Query(value = """
            SELECT l.* FROM lugar l
            WHERE ST_DWithin(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radio)
              AND (:categoria IS NULL OR l.categoria = :categoria)
            ORDER BY ST_Distance(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) ASC
            """, nativeQuery = true)
    List<Lugar> findNearbyWithCategory(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radio") double radio,
            @Param("categoria") String categoria
    );
}
