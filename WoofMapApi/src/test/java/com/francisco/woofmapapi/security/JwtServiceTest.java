package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
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
        String email = jwtService.extraerEmail(token);

        assertThat(email).isEqualTo("juan@woofmap.com");
    }

    @Test
    @DisplayName("Debe extraer el ID de usuario del token")
    void debeExtraerIdDelToken() {
        String token = jwtService.generarToken(usuario);
        Long id = jwtService.extraerUsuarioId(token);

        assertThat(id).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe validar correctamente un token con el email correspondiente")
    void debeValidarTokenCorrectamente() {
        String token = jwtService.generarToken(usuario);

        boolean esValido = jwtService.esTokenValido(token, "juan@woofmap.com");

        assertThat(esValido).isTrue();
    }

    @Test
    @DisplayName("Debe invalidar token si el email no coincide")
    void debeInvalidarTokenSiEmailNoCoincide() {
        String token = jwtService.generarToken(usuario);

        boolean esValido = jwtService.esTokenValido(token, "otro@woofmap.com");

        assertThat(esValido).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar token cuando ha expirado")
    void debeRechazarTokenExpirado() {
        // Servicio con expiración de 0 ms (inmediatamente expirado)
        JwtService shortLivedJwtService = new JwtService(secretKey, -1000);
        String token = shortLivedJwtService.generarToken(usuario);

        boolean esValido = shortLivedJwtService.esTokenValido(token, "juan@woofmap.com");

        assertThat(esValido).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar un token con firma alterada o corrupto")
    void debeRechazarTokenInvalido() {
        String tokenInvalido = "eyJhbGciOiJIUzI1NiJ9.invalido.firma";

        boolean esValido = jwtService.esTokenValido(tokenInvalido, "juan@woofmap.com");

        assertThat(esValido).isFalse();
    }
}
