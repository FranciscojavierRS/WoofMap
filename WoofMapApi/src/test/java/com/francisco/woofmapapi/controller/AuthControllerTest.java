package com.francisco.woofmapapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.francisco.woofmapapi.dto.AuthResponse;
import com.francisco.woofmapapi.dto.LoginRequest;
import com.francisco.woofmapapi.dto.RegisterRequest;
import com.francisco.woofmapapi.exception.EmailAlreadyExistsException;
import com.francisco.woofmapapi.exception.GlobalExceptionHandler;
import com.francisco.woofmapapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/register con datos validos debe devolver 201 Created")
    void debeRegistrarUsuarioYDevolver201() throws Exception {
        RegisterRequest request = new RegisterRequest("Carlos Gomez", "carlos@woofmap.com", "password123");
        AuthResponse response = new AuthResponse("jwt.mock.token", 1L, "Carlos Gomez", "carlos@woofmap.com");

        when(authService.registrar(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.mock.token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.email").value("carlos@woofmap.com"))
                .andExpect(jsonPath("$.nombre").value("Carlos Gomez"));
    }

    @Test
    @DisplayName("POST /auth/register con datos invalidos debe devolver 400 Bad Request")
    void debeDevolver400CuandoRegistroInvalido() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "email-invalido", "123"); // pass corto (<6)

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nombre").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());

        verify(authService, never()).registrar(any());
    }

    @Test
    @DisplayName("POST /auth/register con email ya existente debe devolver 409 Conflict")
    void debeDevolver409SiEmailYaExiste() throws Exception {
        RegisterRequest request = new RegisterRequest("Carlos Gomez", "duplicado@woofmap.com", "password123");

        when(authService.registrar(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("El email ya esta registrado: duplicado@woofmap.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El email ya esta registrado: duplicado@woofmap.com"));
    }

    @Test
    @DisplayName("POST /auth/login con credenciales validas debe devolver 200 OK")
    void debeIniciarSesionYDevolver200() throws Exception {
        LoginRequest request = new LoginRequest("carlos@woofmap.com", "password123");
        AuthResponse response = new AuthResponse("jwt.mock.token", 1L, "Carlos Gomez", "carlos@woofmap.com");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.mock.token"))
                .andExpect(jsonPath("$.usuarioId").value(1));
    }

    @Test
    @DisplayName("POST /auth/login con credenciales invalidas debe devolver 401 Unauthorized")
    void debeDevolver401ConCredencialesInvalidas() throws Exception {
        LoginRequest request = new LoginRequest("carlos@woofmap.com", "passIncorrecta");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }
}
