package com.francisco.woofmapapi.dto;

import jakarta.validation.constraints.NotNull;

public record VotoRequest(
        @NotNull(message = "El voto es obligatorio")
        Boolean positivo
) {
}
