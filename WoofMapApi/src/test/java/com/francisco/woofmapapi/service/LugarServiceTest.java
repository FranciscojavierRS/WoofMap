package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.entity.Foto;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.ComentarioRepository;
import com.francisco.woofmapapi.repository.FotoRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.service.storage.AlmacenamientoFotos;
import com.francisco.woofmapapi.util.GeometryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LugarServiceTest {

    @Mock private LugarRepository lugarRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private FotoRepository fotoRepository;
    @Mock private AlmacenamientoFotos almacenamientoFotos;

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
    @DisplayName("Debe crear un lugar correctamente asociado al usuario autenticado")
    void debeCrearLugarCorrectamente() {
        LugarRequest request = new LugarRequest(
                "Parque Canino del Retiro",
                "Zona amplia vallada con fuentes para perros",
                "parque",
                40.4153,
                -3.6844
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(lugarRepository.save(any(Lugar.class))).thenAnswer(invocation -> {
            Lugar l = invocation.getArgument(0);
            l.setId(10L);
            return l;
        });

        LugarResponse response = lugarService.crearLugar(request, 1L);

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
    @DisplayName("Debe lanzar excepcion al crear lugar si el usuario autenticado no existe")
    void debeLanzarExcepcionAlCrearSiUsuarioNoExiste() {
        LugarRequest request = new LugarRequest(
                "Parque Canino",
                "Descripcion",
                "PARQUE",
                40.4153,
                -3.6844
        );

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lugarService.crearLugar(request, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(lugarRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar crear un lugar sin usuario autenticado")
    void debeRechazarCrearSinUsuario() {
        LugarRequest request = new LugarRequest("Parque", "Desc", "PARQUE", 40.0, -3.0);

        assertThatThrownBy(() -> lugarService.crearLugar(request, null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(lugarRepository);
    }

    @Test
    @DisplayName("Debe obtener un lugar por id si existe, con valoracion media")
    void debeObtenerLugarPorId() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));
        when(comentarioRepository.calcularMediaValoracion(10L)).thenReturn(Optional.of(4.5));
        when(comentarioRepository.countByLugarId(10L)).thenReturn(3L);

        LugarResponse response = lugarService.obtenerPorId(10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nombre()).isEqualTo("Parque Canino del Retiro");
        assertThat(response.valoracionMedia()).isEqualTo(4.5);
        assertThat(response.totalComentarios()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Debe obtener un lugar con valoracion null si no tiene comentarios")
    void debeObtenerLugarSinComentarios() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));
        when(comentarioRepository.calcularMediaValoracion(10L)).thenReturn(Optional.empty());
        when(comentarioRepository.countByLugarId(10L)).thenReturn(0L);

        LugarResponse response = lugarService.obtenerPorId(10L);

        assertThat(response.valoracionMedia()).isNull();
        assertThat(response.totalComentarios()).isEqualTo(0L);
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

        when(lugarRepository.buscarCercanos(latUser, lonUser, radioMetros, null, 200))
                .thenReturn(List.of(lugarMock));

        List<LugarResponse> result = lugarService.listarLugares(latUser, lonUser, radioMetros, null);

        assertThat(result).hasSize(1);
        LugarResponse resp = result.get(0);
        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.distanciaMetros()).isNotNull();
        assertThat(resp.distanciaMetros()).isPositive();
    }

    @Test
    @DisplayName("Debe listar lugares cercanos normalizando la categoria a mayusculas")
    void debeListarLugaresCercanosFiltrandoPorCategoria() {
        double latUser = 40.4168;
        double lonUser = -3.7038;
        double radioMetros = 5000.0;

        when(lugarRepository.buscarCercanos(latUser, lonUser, radioMetros, "PARQUE", 200))
                .thenReturn(List.of(lugarMock));

        List<LugarResponse> result = lugarService.listarLugares(latUser, lonUser, radioMetros, " parque ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoria()).isEqualTo("PARQUE");
    }

    @Test
    @DisplayName("Debe limitar el radio de busqueda a 50 km")
    void debeLimitarRadioMaximo() {
        when(lugarRepository.buscarCercanos(40.4168, -3.7038, 50_000.0, null, 200))
                .thenReturn(List.of(lugarMock));

        lugarService.listarLugares(40.4168, -3.7038, 100_000.0, null);

        verify(lugarRepository).buscarCercanos(40.4168, -3.7038, 50_000.0, null, 200);
    }

    @Test
    @DisplayName("Debe rechazar latitud sin longitud")
    void debeRechazarCoordenadasIncompletas() {
        assertThatThrownBy(() -> lugarService.listarLugares(40.0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lat y lon juntos");

        verifyNoInteractions(lugarRepository);
    }

    @Test
    @DisplayName("Debe rechazar coordenadas fuera de rango")
    void debeRechazarCoordenadasFueraDeRango() {
        assertThatThrownBy(() -> lugarService.listarLugares(95.0, -3.0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fuera de rango");

        verifyNoInteractions(lugarRepository);
    }

    @Test
    @DisplayName("Debe listar todos los lugares con un limite duro")
    void debeListarTodosConLimite() {
        when(lugarRepository.findAllByOrderByIdAsc(any(Pageable.class)))
                .thenReturn(List.of(lugarMock));

        List<LugarResponse> result = lugarService.listarLugares(null, null, null, null);

        assertThat(result).hasSize(1);
        verify(lugarRepository).findAllByOrderByIdAsc(argThat((Pageable p) -> p.getPageSize() == 200));
    }

    @Test
    @DisplayName("Debe editar un lugar existente cuando el usuario autenticado es el creador")
    void debeEditarLugarCuandoEsCreador() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));
        when(lugarRepository.save(any(Lugar.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LugarRequest updateRequest = new LugarRequest(
                "Parque Canino del Retiro Renovado",
                "Nueva zona con agilidad canina",
                "PARQUE",
                40.4150,
                -3.6840
        );

        LugarResponse response = lugarService.editarLugar(10L, updateRequest, 1L);

        assertThat(response.nombre()).isEqualTo("Parque Canino del Retiro Renovado");
        assertThat(response.descripcion()).isEqualTo("Nueva zona con agilidad canina");
        assertThat(response.lat()).isEqualTo(40.4150);
        assertThat(response.lon()).isEqualTo(-3.6840);
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException al editar si el usuario no es el creador")
    void debeLanzarAccessDeniedAlEditarSiNoEsCreador() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));

        LugarRequest updateRequest = new LugarRequest(
                "Parque Modificado", "Descripcion", "PARQUE", 40.4150, -3.6840
        );

        assertThatThrownBy(() -> lugarService.editarLugar(10L, updateRequest, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo el creador puede editarlo");

        verify(lugarRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar un lugar por id cuando el usuario es el creador")
    void debeEliminarLugarCuandoEsCreador() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));

        lugarService.eliminarLugar(10L, 1L);

        verify(lugarRepository).delete(lugarMock);
    }

    @Test
    @DisplayName("Debe borrar los archivos de las fotos antes de eliminar el lugar")
    void debeBorrarFotosAlEliminarLugar() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));
        Foto foto = new Foto(1L, "/uploads/a.jpg", lugarMock, usuarioMock, Instant.now());
        when(fotoRepository.findByLugarId(10L)).thenReturn(List.of(foto));

        lugarService.eliminarLugar(10L, 1L);

        verify(almacenamientoFotos).eliminar("/uploads/a.jpg");
        verify(lugarRepository).delete(lugarMock);
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException al eliminar si el usuario no es el creador")
    void debeLanzarAccessDeniedAlEliminarSiNoEsCreador() {
        when(lugarRepository.findById(10L)).thenReturn(Optional.of(lugarMock));

        assertThatThrownBy(() -> lugarService.eliminarLugar(10L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Solo el creador puede eliminarlo");

        verify(lugarRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe devolver el ranking de lugares por valoracion media")
    void debeListarTopLugares() {
        when(lugarRepository.findTopLugares(isNull(), any(Pageable.class)))
                .thenReturn(java.util.Collections.singletonList(new Object[]{lugarMock, 4.5, 3L}));

        List<LugarResponse> result = lugarService.listarTopLugares(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).valoracionMedia()).isEqualTo(4.5);
        assertThat(result.get(0).totalComentarios()).isEqualTo(3L);
        verify(lugarRepository).findTopLugares(isNull(),
                argThat((Pageable p) -> p.getPageSize() == 20));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException al intentar eliminar un lugar inexistente")
    void debeLanzarExcepcionAlEliminarInexistente() {
        when(lugarRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lugarService.eliminarLugar(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lugar no encontrado");

        verify(lugarRepository, never()).delete(any());
    }
}
