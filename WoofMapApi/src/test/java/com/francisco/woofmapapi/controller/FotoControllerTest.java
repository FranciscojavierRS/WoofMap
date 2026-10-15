package com.francisco.woofmapapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.francisco.woofmapapi.dto.FotoResponse;
import com.francisco.woofmapapi.exception.GlobalExceptionHandler;
import com.francisco.woofmapapi.exception.InvalidFileException;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.service.FotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FotoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private FotoService fotoService;

    @InjectMocks
    private FotoController fotoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fotoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /lugares/{id}/fotos con archivo valido debe devolver 201 Created")
    void subirFoto_archivoValido_devuelve201() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "foto", "perro.jpg", MediaType.IMAGE_JPEG_VALUE, "datos".getBytes()
        );

        FotoResponse response = new FotoResponse(1L, "/uploads/uuid.jpg", 10L, 1L, Instant.now());
        when(fotoService.subirFoto(eq(10L), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/lugares/10/fotos").file(archivo))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/lugares/10/fotos/1"))
                .andExpect(jsonPath("$.url").value("/uploads/uuid.jpg"))
                .andExpect(jsonPath("$.lugarId").value(10));
    }

    @Test
    @DisplayName("POST /lugares/{id}/fotos con archivo invalido debe devolver 400")
    void subirFoto_archivoInvalido_devuelve400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "foto", "virus.exe", "application/x-sh", "bad".getBytes()
        );

        when(fotoService.subirFoto(eq(10L), any(), any()))
                .thenThrow(new InvalidFileException("Tipo de archivo no permitido: application/x-sh"));

        mockMvc.perform(multipart("/lugares/10/fotos").file(archivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /lugares/{id}/fotos con lugar inexistente debe devolver 404")
    void subirFoto_lugarNoExistente_devuelve404() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "foto", "p.jpg", MediaType.IMAGE_JPEG_VALUE, "d".getBytes()
        );
        when(fotoService.subirFoto(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("Lugar no encontrado con id: 99"));

        mockMvc.perform(multipart("/lugares/99/fotos").file(archivo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /lugares/{id}/fotos debe devolver 200 con lista de fotos")
    void listarFotos_lugarExistente_devuelve200() throws Exception {
        FotoResponse f1 = new FotoResponse(1L, "/uploads/a.jpg", 10L, 1L, Instant.now());
        FotoResponse f2 = new FotoResponse(2L, "/uploads/b.jpg", 10L, 2L, Instant.now());
        when(fotoService.listarFotosDeLugar(10L)).thenReturn(List.of(f1, f2));

        mockMvc.perform(get("/lugares/10/fotos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value("/uploads/a.jpg"))
                .andExpect(jsonPath("$[1].url").value("/uploads/b.jpg"));
    }

    @Test
    @DisplayName("GET /lugares/{id}/fotos con lugar inexistente debe devolver 404")
    void listarFotos_lugarNoExistente_devuelve404() throws Exception {
        when(fotoService.listarFotosDeLugar(99L))
                .thenThrow(new ResourceNotFoundException("Lugar no encontrado con id: 99"));

        mockMvc.perform(get("/lugares/99/fotos"))
                .andExpect(status().isNotFound());
    }
}
