package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LugarServiceTest {

    @Mock
    private LugarRepository lugarRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LugarService lugarService;

    private Usuario usuarioMock;
    private Lugar lugarMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario(1L, "Juan", "juan@woofmap.com", "hash123", Instant.now());
        lugarMock = new Lugar(
                10L,
                "Parque Canino del Retiro",
                "Zona amplia vallada con fuentes para perros",
                "PARQUE",
                GeometryUtils.createPoint(40.4153, -3.6844),
                usuarioMock,
                Instant.now()
        );
    }

    @Test
    @DisplayName("Debe crear un lugar correctamente cuando el usuario existe")
    void debeCrearLugarCorrectamente() {
        LugarRequest request = new LugarRequest(
                "Parque Canino del Retiro",
                "Zona amplia vallada con fuentes para perros",
                "PARQUE",
                40.4153,
                -3.6844,
                1L
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(lugarRepository.save(any(Lugar.class))).thenAnswer(invocation -> {
            Lugar l = invocation.getArgument(0);
            l.setId(10L);
            return l;
        });

        LugarResponse response = lugarService.crearLugar(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nombre()).isEqualTo("Parque Canino del Retiro");
        assertThat(response.categoria()).isEqualTo("PARQUE");
        assertThat(response.lat()).isEqualTo(40.4153);
        assertThat(response.lon()).isEqualTo(-3.6844);
        assertThat(response.usuarioId()).isEqualTo(1L);

        verify(lugarRepository).save(any(Lugar.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion al crear lugar si el usuario no existe")
    void debeLanzarExcepcionAlCrearSiUsuarioNoExiste() {
        LugarRequest request = new LugarRequest(
                "Parque Canino",
                "Descripcion",
                "PARQUE",
                40.4153,
                -3.6844,
                999L
        );

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lugarService.crearLugar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(lugarRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener un lugar por id si existe")
    void debeObtenerLugarPorId() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));

        LugarResponse response = lugarService.obtenerPorId(10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nombre()).isEqualTo("Parque Canino del Retiro");
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el lugar no existe por id")
    void debeLanzarExcepcionSiLugarNoExiste() {
        when(lugarRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lugarService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lugar no encontrado");
    }

    @Test
    @DisplayName("Debe listar lugares cercanos con distancia calculada")
    void debeListarLugaresCercanos() {
        double latUser = 40.4168;
        double lonUser = -3.7038;
        double radioMetros = 5000.0;

        when(lugarRepository.findNearby(latUser, lonUser, radioMetros))
                .thenReturn(List.of(lugarMock));

        List<LugarResponse> result = lugarService.listarLugares(latUser, lonUser, radioMetros, null);

        assertThat(result).hasSize(1);
        LugarResponse resp = result.get(0);
        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.distanciaMetros()).isNotNull();
        assertThat(resp.distanciaMetros()).isPositive();
    }

    @Test
    @DisplayName("Debe listar lugares cercanos filtrando por categoria")
    void debeListarLugaresCercanosFiltrandoPorCategoria() {
        double latUser = 40.4168;
        double lonUser = -3.7038;
        double radioMetros = 5000.0;
        String categoria = "PARQUE";

        when(lugarRepository.findNearbyWithCategory(latUser, lonUser, radioMetros, categoria))
                .thenReturn(List.of(lugarMock));

        List<LugarResponse> result = lugarService.listarLugares(latUser, lonUser, radioMetros, categoria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoria()).isEqualTo("PARQUE");
    }

    @Test
    @DisplayName("Debe editar un lugar existente")
    void debeEditarLugar() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));
        when(lugarRepository.save(any(Lugar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LugarRequest updateRequest = new LugarRequest(
                "Parque Canino del Retiro Renovado",
                "Nueva zona con agilidad canina",
                "PARQUE",
                40.4150,
                -3.6840,
                1L
        );

        LugarResponse response = lugarService.editarLugar(10L, updateRequest);

        assertThat(response.nombre()).isEqualTo("Parque Canino del Retiro Renovado");
        assertThat(response.descripcion()).isEqualTo("Nueva zona con agilidad canina");
        assertThat(response.lat()).isEqualTo(40.4150);
        assertThat(response.lon()).isEqualTo(-3.6840);
    }

    @Test
    @DisplayName("Debe eliminar un lugar por id")
    void debeEliminarLugar() {
        when(lugarRepository.existsById(10L)).thenReturn(true);

        lugarService.eliminarLugar(10L);

        verify(lugarRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al intentar eliminar un lugar inexistente")
    void debeLanzarExcepcionAlEliminarInexistente() {
        when(lugarRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> lugarService.eliminarLugar(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lugar no encontrado");

        verify(lugarRepository, never()).deleteById(any());
    }
}
