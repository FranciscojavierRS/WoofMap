package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.ComentarioRequest;
import com.francisco.woofmapapi.dto.ComentarioResponse;
import com.francisco.woofmapapi.entity.Comentario;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.ComentarioRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private LugarRepository lugarRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ComentarioService comentarioService;

    private Lugar lugar;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Carlos");
        usuario.setEmail("carlos@example.com");

        lugar = new Lugar();
        lugar.setId(10L);
        lugar.setNombre("Parque Central");
        lugar.setUsuario(usuario);
    }

    @Test
    @DisplayName("crearComentario: guarda y devuelve ComentarioResponse")
    void crearComentario_parametrosValidos_devuelveResponse() {
        ComentarioRequest req = new ComentarioRequest("Muy bonito lugar", (short) 5);
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugar));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Comentario guardado = new Comentario(7L, "Muy bonito lugar", (short) 5, lugar, usuario, Instant.now());
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(guardado);

        ComentarioResponse response = comentarioService.crearComentario(10L, req, 1L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.texto()).isEqualTo("Muy bonito lugar");
        assertThat(response.valoracion()).isEqualTo((short) 5);
        assertThat(response.lugarId()).isEqualTo(10L);
        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test
    @DisplayName("crearComentario: lanza AccessDeniedException si no hay usuarioId")
    void crearComentario_sinUsuario_lanzaAccessDeniedException() {
        ComentarioRequest req = new ComentarioRequest("test", (short) 3);
        assertThatThrownBy(() -> comentarioService.crearComentario(10L, req, null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(comentarioRepository);
    }

    @Test
    @DisplayName("crearComentario: lanza ResourceNotFoundException si el lugar no existe")
    void crearComentario_lugarNoExiste_lanzaResourceNotFoundException() {
        when(lugarRepository.findById(99L)).thenReturn(Optional.empty());
        ComentarioRequest req = new ComentarioRequest("test", (short) 3);

        assertThatThrownBy(() -> comentarioService.crearComentario(99L, req, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("listarComentarios: devuelve lista ordenada de ComentarioResponse")
    void listarComentarios_lugarConComentarios_devuelveLista() {
        when(lugarRepository.existsById(10L)).thenReturn(true);
        Comentario c1 = new Comentario(1L, "Genial", (short) 5, lugar, usuario, Instant.now());
        Comentario c2 = new Comentario(2L, "Bien", (short) 3, lugar, usuario, Instant.now());
        when(comentarioRepository.findByLugarIdOrderByFechaDesc(10L)).thenReturn(List.of(c1, c2));

        List<ComentarioResponse> result = comentarioService.listarComentarios(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).texto()).isEqualTo("Genial");
        assertThat(result.get(1).valoracion()).isEqualTo((short) 3);
    }

    @Test
    @DisplayName("listarComentarios: lanza ResourceNotFoundException si el lugar no existe")
    void listarComentarios_lugarNoExiste_lanzaResourceNotFoundException() {
        when(lugarRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> comentarioService.listarComentarios(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("calcularMediaValoracion: devuelve la media si hay comentarios")
    void calcularMediaValoracion_conComentarios_devuelveMedia() {
        when(comentarioRepository.calcularMediaValoracion(10L)).thenReturn(Optional.of(4.2));
        Double media = comentarioService.calcularMediaValoracion(10L);
        assertThat(media).isEqualTo(4.2);
    }

    @Test
    @DisplayName("calcularMediaValoracion: devuelve null si no hay comentarios")
    void calcularMediaValoracion_sinComentarios_devuelveNull() {
        when(comentarioRepository.calcularMediaValoracion(10L)).thenReturn(Optional.empty());
        assertThat(comentarioService.calcularMediaValoracion(10L)).isNull();
    }

    @Test
    @DisplayName("contarComentarios: devuelve el total correcto")
    void contarComentarios_devuelveTotal() {
        when(comentarioRepository.countByLugarId(10L)).thenReturn(5L);
        assertThat(comentarioService.contarComentarios(10L)).isEqualTo(5L);
    }
}
