package com.francisco.woofmapapi.dto;

import java.time.Instant;

public record PerroResponse(
        Long id,
        String nombre,
        String url,
        Long usuarioId,
        String usuarioNombre,
        Instant fechaSubida,
        long likes,
        long dislikes
) {
}
