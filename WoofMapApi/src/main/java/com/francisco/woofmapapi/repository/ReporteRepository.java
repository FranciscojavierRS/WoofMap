package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByOrderByResueltoAscFechaDesc();

    boolean existsByUsuarioIdAndTipoAndReferenciaIdAndResueltoFalse(
            Long usuarioId, String tipo, Long referenciaId);
}
