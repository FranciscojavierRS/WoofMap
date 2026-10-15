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
        Instant fechaCreacion,
        Double distanciaMetros,
        Double valoracionMedia,
        Long totalComentarios
) {
    // Constructor sin distancia, sin valoración (búsqueda básica desde Fase 1/2)
    public LugarResponse(Long id, String nombre, String descripcion, String categoria,
                         Double lat, Double lon, Long usuarioId, Instant fechaCreacion) {
        this(id, nombre, descripcion, categoria, lat, lon, usuarioId, fechaCreacion, null, null, null);
    }

    // Constructor con distancia pero sin valoración
    public LugarResponse(Long id, String nombre, String descripcion, String categoria,
                         Double lat, Double lon, Long usuarioId, Instant fechaCreacion,
                         Double distanciaMetros) {
        this(id, nombre, descripcion, categoria, lat, lon, usuarioId, fechaCreacion, distanciaMetros, null, null);
    }
}
