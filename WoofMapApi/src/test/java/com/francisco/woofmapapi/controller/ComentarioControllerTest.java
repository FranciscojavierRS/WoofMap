package com.francisco.woofmapapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.francisco.woofmapapi.dto.ComentarioRequest;
import com.francisco.woofmapapi.dto.ComentarioResponse;
import com.francisco.woofmapapi.exception.GlobalExceptionHandler;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.service.ComentarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComentarioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ComentarioService comentarioService;

    @InjectMocks
    private ComentarioController comentarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(comentarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /lugares/{id}/comentarios con datos validos devuelve 201")
    void crearComentario_datosValidos_devuelve201() throws Exception {
        ComentarioRequest req = new ComentarioRequest("Muy buen lugar para perros", (short) 5);
        ComentarioResponse resp = new ComentarioResponse(1L, "Muy buen lugar para perros", (short) 5, 10L, 1L, Instant.now());

        when(comentarioService.crearComentario(eq(10L), any(ComentarioRequest.class), any()))
                .thenReturn(resp);

        mockMvc.perform(post("/lugares/10/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.texto").value("Muy buen lugar para perros"))
                .andExpect(jsonPath("$.valoracion").value(5));
    }

    @Test
    @DisplayName("POST /lugares/{id}/comentarios con valoracion fuera de rango devuelve 400")
    void crearComentario_valoracionInvalida_devuelve400() throws Exception {
        ComentarioRequest req = new ComentarioRequest("Texto", (short) 6); // valoracion > 5

        mockMvc.perform(post("/lugares/10/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.valoracion").exists());
    }

    @Test
    @DisplayName("POST /lugares/{id}/comentarios con texto vacio devuelve 400")
    void crearComentario_textoVacio_devuelve400() throws Exception {
        ComentarioRequest req = new ComentarioRequest("", (short) 3);

        mockMvc.perform(post("/lugares/10/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.texto").exists());
    }

    @Test
    @DisplayName("POST /lugares/{id}/comentarios con lugar inexistente devuelve 404")
    void crearComentario_lugarNoExiste_devuelve404() throws Exception {
        ComentarioRequest req = new ComentarioRequest("Test", (short) 3);
        when(comentarioService.crearComentario(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("Lugar no encontrado con id: 99"));

        mockMvc.perform(post("/lugares/99/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /lugares/{id}/comentarios devuelve 200 con lista")
    void listarComentarios_lugarExistente_devuelve200() throws Exception {
        ComentarioResponse c1 = new ComentarioResponse(1L, "Genial", (short) 5, 10L, 1L, Instant.now());
        ComentarioResponse c2 = new ComentarioResponse(2L, "Bien", (short) 3, 10L, 2L, Instant.now());
        when(comentarioService.listarComentarios(10L)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/lugares/10/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].valoracion").value(5))
                .andExpect(jsonPath("$[1].texto").value("Bien"));
    }

    @Test
    @DisplayName("GET /lugares/{id}/comentarios con lugar inexistente devuelve 404")
    void listarComentarios_lugarNoExiste_devuelve404() throws Exception {
        when(comentarioService.listarComentarios(99L))
                .thenThrow(new ResourceNotFoundException("Lugar no encontrado con id: 99"));

        mockMvc.perform(get("/lugares/99/comentarios"))
                .andExpect(status().isNotFound());
    }
}
