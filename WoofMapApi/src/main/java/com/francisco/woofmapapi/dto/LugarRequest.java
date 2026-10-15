package com.francisco.woofmapapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LugarRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String nombre,

        String descripcion,

        @NotBlank(message = "La categoria es obligatoria")
        @Size(max = 50, message = "La categoria no puede exceder los 50 caracteres")
        String categoria,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90.0")
        @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90.0")
        Double lat,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180.0")
        @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180.0")
        Double lon,

        Long usuarioId
) {
}
