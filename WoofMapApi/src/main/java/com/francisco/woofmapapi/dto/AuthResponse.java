package com.francisco.woofmapapi.dto;

public record AuthResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nombre,
        String email
) {
    public AuthResponse(String token, Long usuarioId, String nombre, String email) {
        this(token, "Bearer", usuarioId, nombre, email);
    }
}
