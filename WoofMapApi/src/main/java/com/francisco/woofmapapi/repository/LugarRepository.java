package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Lugar;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {

    List<Lugar> findByCategoria(String categoria, Pageable pageable);

    List<Lugar> findAllByOrderByIdAsc(Pageable pageable);

    // Sin GROUP BY a proposito: agrupar por la entidad incluiria la columna geography.
    @Query("""
            SELECT l,
                   (SELECT AVG(CAST(c.valoracion AS double)) FROM Comentario c WHERE c.lugar = l) AS media,
                   (SELECT COUNT(c) FROM Comentario c WHERE c.lugar = l) AS total
            FROM Lugar l
            WHERE (:categoria IS NULL OR l.categoria = :categoria)
            ORDER BY media DESC NULLS LAST, total DESC, l.id ASC
            """)
    List<Object[]> findTopLugares(@Param("categoria") String categoria, Pageable pageable);

    @Query(value = """
            SELECT l.* FROM lugar l
            WHERE ST_DWithin(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radio)
              AND (:categoria IS NULL OR l.categoria = :categoria)
            ORDER BY ST_Distance(l.ubicacion, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) ASC
            LIMIT :limite
            """, nativeQuery = true)
    List<Lugar> buscarCercanos(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radio") double radio,
            @Param("categoria") String categoria,
            @Param("limite") int limite
    );
}
