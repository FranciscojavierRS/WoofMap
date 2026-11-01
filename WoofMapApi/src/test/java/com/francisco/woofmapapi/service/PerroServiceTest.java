package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.PerroResponse;
import com.francisco.woofmapapi.dto.VotoRequest;
import com.francisco.woofmapapi.entity.Perro;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.entity.VotoPerro;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.PerroRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.repository.VotoPerroRepository;
import com.francisco.woofmapapi.service.storage.AlmacenamientoFotos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerroServiceTest {

    @Mock private PerroRepository perroRepository;
    @Mock private VotoPerroRepository votoPerroRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AlmacenamientoFotos almacenamientoFotos;

    @InjectMocks
    private PerroService perroService;

    private Usuario usuario;
    private Perro perro;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Ana", "ana@example.com", "hash", Instant.now());
        perro = new Perro(5L, "Toby", "/uploads/toby.jpg", usuario, Instant.now());
    }

    @Test
    @DisplayName("subirPerro: guarda el perro y devuelve su respuesta")
    void subirPerro_valido_devuelvePerro() {
        MockMultipartFile archivo = new MockMultipartFile("foto", "toby.jpg", "image/jpeg", "img".getBytes());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(almacenamientoFotos.guardar(archivo)).thenReturn("/uploads/toby.jpg");
        when(perroRepository.save(any(Perro.class))).thenReturn(perro);

        PerroResponse response = perroService.subirPerro(archivo, "  Toby  ", 1L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.nombre()).isEqualTo("Toby");
        assertThat(response.usuarioId()).isEqualTo(1L);
        verify(perroRepository).save(any(Perro.class));
    }

    @Test
    @DisplayName("subirPerro: sin usuario lanza AccessDeniedException")
    void subirPerro_sinUsuario_lanzaAccessDenied() {
        MockMultipartFile archivo = new MockMultipartFile("foto", "toby.jpg", "image/jpeg", "img".getBytes());

        assertThatThrownBy(() -> perroService.subirPerro(archivo, "Toby", null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(perroRepository, almacenamientoFotos);
    }

    @Test
    @DisplayName("subirPerro: nombre vacio lanza IllegalArgumentException")
    void subirPerro_sinNombre_lanzaIllegalArgument() {
        MockMultipartFile archivo = new MockMultipartFile("foto", "toby.jpg", "image/jpeg", "img".getBytes());

        assertThatThrownBy(() -> perroService.subirPerro(archivo, "   ", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");

        verifyNoInteractions(almacenamientoFotos);
    }

    @Test
    @DisplayName("ranking: devuelve likes y dislikes calculados por el repositorio")
    void ranking_devuelveContadores() {
        when(perroRepository.ranking(any(Instant.class), any(Pageable.class)))
                .thenReturn(java.util.Collections.singletonList(new Object[]{perro, 4L, 2L}));

        List<PerroResponse> result = perroService.ranking("SEMANA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).likes()).isEqualTo(4);
        assertThat(result.get(0).dislikes()).isEqualTo(2);
    }

    @Test
    @DisplayName("votar: registra un like de otro usuario")
    void votar_otroPerro_guardaVoto() {
        Usuario votante = new Usuario(2L, "Luis", "luis@example.com", "hash", Instant.now());
        when(perroRepository.findById(5L)).thenReturn(Optional.of(perro));
        when(votoPerroRepository.existsByPerroIdAndUsuarioId(5L, 2L)).thenReturn(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(votante));

        perroService.votar(5L, new VotoRequest(true), 2L);

        verify(votoPerroRepository).save(any(VotoPerro.class));
    }

    @Test
    @DisplayName("votar: no permite valorar el propio perro")
    void votar_propioPerro_lanzaIllegalArgument() {
        when(perroRepository.findById(5L)).thenReturn(Optional.of(perro));

        assertThatThrownBy(() -> perroService.votar(5L, new VotoRequest(true), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("propio");

        verify(votoPerroRepository, never()).save(any());
    }

    @Test
    @DisplayName("votar: no permite votar dos veces")
    void votar_duplicado_lanzaIllegalArgument() {
        when(perroRepository.findById(5L)).thenReturn(Optional.of(perro));
        when(votoPerroRepository.existsByPerroIdAndUsuarioId(5L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> perroService.votar(5L, new VotoRequest(false), 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya has valorado");

        verify(votoPerroRepository, never()).save(any());
    }

    @Test
    @DisplayName("votar: perro inexistente lanza ResourceNotFoundException")
    void votar_perroInexistente_lanzaResourceNotFound() {
        when(perroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perroService.votar(99L, new VotoRequest(true), 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("mazo: exige usuario autenticado")
    void mazo_sinUsuario_lanzaAccessDenied() {
        assertThatThrownBy(() -> perroService.mazo(null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(perroRepository);
    }
}
