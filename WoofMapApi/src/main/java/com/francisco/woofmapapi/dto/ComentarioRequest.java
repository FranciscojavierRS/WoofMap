package com.francisco.woofmapapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

        @NotBlank(message = "El texto del comentario no puede estar vacio")
        @Size(max = 2000, message = "El texto no puede superar los 2000 caracteres")
        String texto,

        @NotNull(message = "La valoracion es obligatoria")
        @Min(value = 1, message = "La valoracion minima es 1")
        @Max(value = 5, message = "La valoracion maxima es 5")
        Short valoracion
) {}
