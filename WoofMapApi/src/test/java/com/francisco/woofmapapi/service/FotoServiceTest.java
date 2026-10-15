package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.FotoResponse;
import com.francisco.woofmapapi.entity.Foto;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.FotoRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.service.storage.AlmacenamientoFotos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FotoServiceTest {

    @Mock private FotoRepository fotoRepository;
    @Mock private LugarRepository lugarRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AlmacenamientoFotos almacenamientoFotos;

    @InjectMocks
    private FotoService fotoService;

    private Lugar lugarExistente;
    private Usuario usuarioExistente;

    @BeforeEach
    void setUp() {
        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNombre("Ana");
        usuarioExistente.setEmail("ana@example.com");

        lugarExistente = new Lugar();
        lugarExistente.setId(10L);
        lugarExistente.setNombre("Parque del Retiro");
        lugarExistente.setUsuario(usuarioExistente);
    }

    @Test
    @DisplayName("subirFoto: guarda y devuelve FotoResponse cuando todo es correcto")
    void subirFoto_parametrosValidos_devuelveFotoResponse() {
        MockMultipartFile archivo = new MockMultipartFile("foto", "perro.jpg", "image/jpeg", "img".getBytes());
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarExistente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(almacenamientoFotos.guardar(archivo)).thenReturn("/uploads/uuid.jpg");

        Foto fotoGuardada = new Foto(5L, "/uploads/uuid.jpg", lugarExistente, usuarioExistente, Instant.now());
        when(fotoRepository.save(any(Foto.class))).thenReturn(fotoGuardada);

        FotoResponse response = fotoService.subirFoto(10L, archivo, 1L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.url()).isEqualTo("/uploads/uuid.jpg");
        assertThat(response.lugarId()).isEqualTo(10L);
        assertThat(response.usuarioId()).isEqualTo(1L);
        verify(almacenamientoFotos).guardar(archivo);
        verify(fotoRepository).save(any(Foto.class));
    }

    @Test
    @DisplayName("subirFoto: lanza AccessDeniedException si no hay usuarioId")
    void subirFoto_sinUsuarioId_lanzaAccessDeniedException() {
        MockMultipartFile archivo = new MockMultipartFile("foto", "p.jpg", "image/jpeg", "x".getBytes());
        assertThatThrownBy(() -> fotoService.subirFoto(10L, archivo, null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(fotoRepository, almacenamientoFotos);
    }

    @Test
    @DisplayName("subirFoto: lanza ResourceNotFoundException si el lugar no existe")
    void subirFoto_lugarNoExistente_lanzaResourceNotFoundException() {
        when(lugarRepository.findById(99L)).thenReturn(Optional.empty());
        MockMultipartFile archivo = new MockMultipartFile("foto", "p.jpg", "image/jpeg", "x".getBytes());

        assertThatThrownBy(() -> fotoService.subirFoto(99L, archivo, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listarFotosDeLugar: devuelve lista de FotoResponse")
    void listarFotosDeLugar_lugarExistente_devuelveLista() {
        when(lugarRepository.existsById(10L)).thenReturn(true);
        Foto f1 = new Foto(1L, "/uploads/a.jpg", lugarExistente, usuarioExistente, Instant.now());
        Foto f2 = new Foto(2L, "/uploads/b.jpg", lugarExistente, usuarioExistente, Instant.now());
        when(fotoRepository.findByLugarIdOrderByFechaSubidaDesc(10L)).thenReturn(List.of(f1, f2));

        List<FotoResponse> result = fotoService.listarFotosDeLugar(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).url()).isEqualTo("/uploads/a.jpg");
    }

    @Test
    @DisplayName("listarFotosDeLugar: lanza ResourceNotFoundException si lugar no existe")
    void listarFotosDeLugar_lugarNoExistente_lanzaResourceNotFoundException() {
        when(lugarRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> fotoService.listarFotosDeLugar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
