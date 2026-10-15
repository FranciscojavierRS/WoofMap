package com.francisco.woofmapapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.exception.GlobalExceptionHandler;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.service.LugarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LugarControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LugarService lugarService;

    @InjectMocks
    private LugarController lugarController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lugarController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /lugares con datos validos debe devolver 201 Created")
    void debeCrearLugarYDevolver201() throws Exception {
        LugarRequest request = new LugarRequest(
                "Pipican Parque Central",
                "Zona acotada con sombra",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L
        );

        LugarResponse response = new LugarResponse(
                1L,
                "Pipican Parque Central",
                "Zona acotada con sombra",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L,
                Instant.now(),
                null
        );

        when(lugarService.crearLugar(any(LugarRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/lugares/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pipican Parque Central"))
                .andExpect(jsonPath("$.categoria").value("PIPICAN"))
                .andExpect(jsonPath("$.lat").value(40.4168))
                .andExpect(jsonPath("$.lon").value(-3.7038));
    }

    @Test
    @DisplayName("POST /lugares con datos invalidos debe devolver 400 Bad Request")
    void debeDevolver400CuandoDatosInvalidos() throws Exception {
        LugarRequest invalidRequest = new LugarRequest(
                "", // nombre vacio
                "Descripcion",
                "", // categoria vacia
                95.0, // latitud invalida (> 90)
                -200.0, // longitud invalida (< -180)
                null
        );

        mockMvc.perform(post("/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.nombre").exists())
                .andExpect(jsonPath("$.validationErrors.categoria").exists())
                .andExpect(jsonPath("$.validationErrors.lat").exists())
                .andExpect(jsonPath("$.validationErrors.lon").exists());

        verify(lugarService, never()).crearLugar(any(), any());
    }

    @Test
    @DisplayName("GET /lugares/{id} existente debe devolver 200 OK")
    void debeDevolverLugarPorId() throws Exception {
        LugarResponse response = new LugarResponse(
                1L,
                "Pipican Parque Central",
                "Zona acotada con sombra",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L,
                Instant.now(),
                null
        );

        when(lugarService.obtenerPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/lugares/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pipican Parque Central"));
    }

    @Test
    @DisplayName("GET /lugares/{id} inexistente debe devolver 404 Not Found")
    void debeDevolver404SiLugarNoExiste() throws Exception {
        when(lugarService.obtenerPorId(999L)).thenThrow(new ResourceNotFoundException("Lugar no encontrado con id: 999"));

        mockMvc.perform(get("/lugares/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Lugar no encontrado con id: 999"));
    }

    @Test
    @DisplayName("GET /lugares con parametros de proximidad debe devolver 200 y lista")
    void debeListarLugaresCercanos() throws Exception {
        LugarResponse resp = new LugarResponse(
                1L,
                "Pipican Parque Central",
                "Zona acotada",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L,
                Instant.now(),
                250.5
        );

        when(lugarService.listarLugares(40.4168, -3.7038, 5000.0, "PIPICAN"))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/lugares")
                        .param("lat", "40.4168")
                        .param("lon", "-3.7038")
                        .param("radio", "5000")
                        .param("categoria", "PIPICAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].distanciaMetros").value(250.5));
    }

    @Test
    @DisplayName("PUT /lugares/{id} con datos validos debe devolver 200 OK")
    void debeActualizarLugar() throws Exception {
        LugarRequest updateRequest = new LugarRequest(
                "Pipican Renovado",
                "Con mas obstaculos",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L
        );

        LugarResponse updatedResponse = new LugarResponse(
                1L,
                "Pipican Renovado",
                "Con mas obstaculos",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L,
                Instant.now(),
                null
        );

        when(lugarService.editarLugar(eq(1L), any(LugarRequest.class), any())).thenReturn(updatedResponse);

        mockMvc.perform(put("/lugares/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pipican Renovado"))
                .andExpect(jsonPath("$.descripcion").value("Con mas obstaculos"));
    }

    @Test
    @DisplayName("PUT /lugares/{id} cuando el usuario no es creador debe devolver 403 Forbidden")
    void debeDevolver403AlEditarSiNoEsCreador() throws Exception {
        LugarRequest updateRequest = new LugarRequest(
                "Pipican Renovado",
                "Con mas obstaculos",
                "PIPICAN",
                40.4168,
                -3.7038,
                1L
        );

        when(lugarService.editarLugar(eq(1L), any(LugarRequest.class), any()))
                .thenThrow(new AccessDeniedException("No tienes permisos para modificar este lugar. Solo el creador puede editarlo."));

        mockMvc.perform(put("/lugares/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No tienes permisos para modificar este lugar. Solo el creador puede editarlo."));
    }

    @Test
    @DisplayName("DELETE /lugares/{id} debe devolver 204 No Content")
    void debeEliminarLugar() throws Exception {
        doNothing().when(lugarService).eliminarLugar(eq(1L), any());

        mockMvc.perform(delete("/lugares/1"))
                .andExpect(status().isNoContent());

        verify(lugarService).eliminarLugar(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /lugares/{id} cuando el usuario no es creador debe devolver 403 Forbidden")
    void debeDevolver403AlEliminarSiNoEsCreador() throws Exception {
        doThrow(new AccessDeniedException("No tienes permisos para eliminar este lugar. Solo el creador puede eliminarlo."))
                .when(lugarService).eliminarLugar(eq(1L), any());

        mockMvc.perform(delete("/lugares/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("No tienes permisos para eliminar este lugar. Solo el creador puede eliminarlo."));
    }
}
