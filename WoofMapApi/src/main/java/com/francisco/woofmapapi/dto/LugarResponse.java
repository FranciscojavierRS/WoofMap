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
        Double distanciaMetros
) {
    public LugarResponse(Long id, String nombre, String descripcion, String categoria, Double lat, Double lon, Long usuarioId, Instant fechaCreacion) {
        this(id, nombre, descripcion, categoria, lat, lon, usuarioId, fechaCreacion, null);
    }
}
