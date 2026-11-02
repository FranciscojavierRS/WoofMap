package com.francisco.woofmapapi.dto;

import java.time.Instant;

/** Perfil del usuario. El email es el identificador de la cuenta y no se puede editar. */
public record PerfilResponse(
        Long id,
        String nombre,
        String email,
        Instant fechaAlta,
        boolean administrador
) {}
