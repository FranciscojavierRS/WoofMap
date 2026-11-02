package com.francisco.woofmapapi.dto;

import java.time.Instant;

public record ReporteResponse(
        Long id,
        String tipo,
        Long referenciaId,
        String motivo,
        boolean resuelto,
        Instant fecha,
        Long usuarioId,
        String usuarioNombre
) {}
