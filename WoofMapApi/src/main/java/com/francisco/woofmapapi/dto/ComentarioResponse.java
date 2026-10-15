package com.francisco.woofmapapi.dto;

import java.time.Instant;

public record ComentarioResponse(
        Long id,
        String texto,
        Short valoracion,
        Long lugarId,
        Long usuarioId,
        Instant fecha
) {}
