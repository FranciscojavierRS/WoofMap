package com.francisco.woofmapapi.dto;

import java.time.Instant;

public record FotoResponse(
        Long id,
        String url,
        Long lugarId,
        Long usuarioId,
        Instant fechaSubida
) {}
