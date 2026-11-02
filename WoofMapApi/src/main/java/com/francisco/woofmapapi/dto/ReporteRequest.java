package com.francisco.woofmapapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReporteRequest(
        @NotBlank(message = "El tipo es obligatorio")
        String tipo,

        @NotNull(message = "El id del contenido es obligatorio")
        Long referenciaId,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
        String motivo
) {}
