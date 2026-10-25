package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.AuthResponse;
import com.francisco.woofmapapi.dto.LoginRequest;
import com.francisco.woofmapapi.dto.RegisterRequest;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.EmailAlreadyExistsException;
import com.francisco.woofmapapi.exception.TooManyRequestsException;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario(
                1L,
                "Carlos Gomez",
                "carlos@woofmap.com",
                "$2a$10$hashedPassword",
                Instant.now()
        );
    }

    @Test
    @DisplayName("Debe registrar un nuevo usuario exitosamente y devolver token JWT")
    void debeRegistrarUsuarioExitosamente() {
        RegisterRequest request = new RegisterRequest("Carlos Gomez", "carlos@woofmap.com", "password123");

        when(usuarioRepository.existsByEmail("carlos@woofmap.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenReturn(usuarioMock);
        when(jwtService.generarToken(usuarioMock)).thenReturn("jwt.token.mock");

        AuthResponse response = authService.registrar(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt.token.mock");
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.usuarioId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("carlos@woofmap.com");
        assertThat(response.nombre()).isEqualTo("Carlos Gomez");

        verify(usuarioRepository).saveAndFlush(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar EmailAlreadyExistsException si el email ya esta registrado")
    void debeLanzarExcepcionSiEmailYaExiste() {
        RegisterRequest request = new RegisterRequest("Carlos Gomez", "carlos@woofmap.com", "password123");

        when(usuarioRepository.existsByEmail("carlos@woofmap.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("El email ya esta registrado");

        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Debe iniciar sesion exitosamente con credenciales validas")
    void debeIniciarSesionExitosamente() {
        LoginRequest request = new LoginRequest("carlos@woofmap.com", "password123");

        when(usuarioRepository.findByEmail("carlos@woofmap.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("password123", usuarioMock.getPasswordHash())).thenReturn(true);
        when(jwtService.generarToken(usuarioMock)).thenReturn("jwt.token.mock");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt.token.mock");
        assertThat(response.usuarioId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("carlos@woofmap.com");
    }

    @Test
    @DisplayName("Debe lanzar BadCredentialsException si el usuario no existe en login")
    void debeLanzarExcepcionSiUsuarioNoExisteEnLogin() {
        LoginRequest request = new LoginRequest("noexiste@woofmap.com", "password123");

        when(usuarioRepository.findByEmail("noexiste@woofmap.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debe lanzar BadCredentialsException si la contrasena es incorrecta en login")
    void debeLanzarExcepcionSiPasswordIncorrectaEnLogin() {
        LoginRequest request = new LoginRequest("carlos@woofmap.com", "passwordIncorrecta");

        when(usuarioRepository.findByEmail("carlos@woofmap.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("passwordIncorrecta", usuarioMock.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales invalidas");

        verify(jwtService, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debe bloquear el login tras 5 intentos fallidos seguidos")
    void debeBloquearTrasCincoIntentosFallidos() {
        LoginRequest request = new LoginRequest("carlos@woofmap.com", "passwordIncorrecta");

        when(usuarioRepository.findByEmail("carlos@woofmap.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("passwordIncorrecta", usuarioMock.getPasswordHash())).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Demasiados intentos");
    }
}
