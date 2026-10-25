package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 86400000; // 24 horas

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secretKey, expirationMs);
        usuario = new Usuario(1L, "Juan Perez", "juan@woofmap.com", "hash123", Instant.now());
    }

    @Test
    @DisplayName("Debe generar un token JWT valido para un usuario")
    void debeGenerarTokenValido() {
        String token = jwtService.generarToken(usuario);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Debe extraer el email (subject) del token")
    void debeExtraerEmailDelToken() {
        String token = jwtService.generarToken(usuario);
        Claims claims = jwtService.extraerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("juan@woofmap.com");
    }

    @Test
    @DisplayName("Debe validar correctamente un token con el email correspondiente")
    void debeValidarTokenCorrectamente() {
        String token = jwtService.generarToken(usuario);
        Claims claims = jwtService.extraerClaims(token);

        assertThat(jwtService.esTokenValido(claims, "juan@woofmap.com")).isTrue();
    }

    @Test
    @DisplayName("Debe invalidar token si el email no coincide")
    void debeInvalidarTokenSiEmailNoCoincide() {
        String token = jwtService.generarToken(usuario);
        Claims claims = jwtService.extraerClaims(token);

        assertThat(jwtService.esTokenValido(claims, "otro@woofmap.com")).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar token cuando ha expirado")
    void debeRechazarTokenExpirado() {
        // Servicio con expiracion de -1000 ms (inmediatamente expirado)
        JwtService shortLivedJwtService = new JwtService(secretKey, -1000);
        String token = shortLivedJwtService.generarToken(usuario);

        assertThatThrownBy(() -> shortLivedJwtService.extraerClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Debe rechazar un token con firma alterada o corrupto")
    void debeRechazarTokenInvalido() {
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.invalido.firma";

        assertThatThrownBy(() -> jwtService.extraerClaims(tokenInvalido))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Debe fallar al arrancar si falta el secreto")
    void debeFallarSiFaltaElSecreto() {
        assertThatThrownBy(() -> new JwtService("", expirationMs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("Debe fallar al arrancar si el secreto es demasiado corto")
    void debeFallarSiElSecretoEsCorto() {
        assertThatThrownBy(() -> new JwtService("corto", expirationMs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }
}
