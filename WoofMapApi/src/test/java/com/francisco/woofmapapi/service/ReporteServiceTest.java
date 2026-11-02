package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.ReporteRequest;
import com.francisco.woofmapapi.dto.ReporteResponse;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Reporte;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.repository.ComentarioRepository;
import com.francisco.woofmapapi.repository.FotoRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.ReporteRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private ReporteRepository reporteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LugarRepository lugarRepository;
    @Mock private FotoRepository fotoRepository;
    @Mock private ComentarioRepository comentarioRepository;

    @InjectMocks
    private ReporteService reporteService;

    private Usuario autor;
    private Usuario otro;

    @BeforeEach
    void setUp() {
        autor = new Usuario(1L, "Autor", "autor@woofmap.com", "hash", Instant.now());
        otro = new Usuario(2L, "Otro", "otro@woofmap.com", "hash", Instant.now());
    }

    @Test
    @DisplayName("crear: guarda el reporte de un lugar ajeno")
    void crear_lugarAjeno_guarda() {
        Lugar lugar = new Lugar(10L, "Parque", "Desc", "PARQUE", null, autor, Instant.now());
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugar));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otro));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(inv -> {
            Reporte r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        ReporteResponse response = reporteService.crear(2L,
                new ReporteRequest("lugar", 10L, "No es dog-friendly"));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.tipo()).isEqualTo("LUGAR");
        assertThat(response.usuarioId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("crear: no permite reportar contenido propio")
    void crear_contenidoPropio_deniega() {
        Lugar lugar = new Lugar(10L, "Parque", "Desc", "PARQUE", null, autor, Instant.now());
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugar));

        assertThatThrownBy(() -> reporteService.crear(1L,
                new ReporteRequest("LUGAR", 10L, "Motivo")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(reporteRepository, never()).save(any());
    }

    @Test
    @DisplayName("listar: sin rol admin deniega el acceso")
    void listar_sinAdmin_deniega() {
        assertThatThrownBy(() -> reporteService.listar())
                .isInstanceOf(AccessDeniedException.class);
    }
}
