package com.francisco.woofmapapi.dto;

import java.time.Instant;

public record LugarResponse(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        Double lat,
        Double lon,
        Long usuarioId,
        String usuarioNombre,
        Instant fechaCreacion,
        Double distanciaMetros,
        Double valoracionMedia,
        Long totalComentarios
) {
}
