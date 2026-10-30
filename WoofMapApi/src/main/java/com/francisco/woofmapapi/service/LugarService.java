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
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class LugarService {

    private static final Logger log = LoggerFactory.getLogger(LugarService.class);

    private static final int LIMITE_LISTADO = 200;
    private static final double RADIO_MAX_METROS = 50_000.0;

    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final FotoRepository fotoRepository;
    private final AlmacenamientoFotos almacenamientoFotos;

    public LugarService(LugarRepository lugarRepository,
                        UsuarioRepository usuarioRepository,
                        ComentarioRepository comentarioRepository,
                        FotoRepository fotoRepository,
                        AlmacenamientoFotos almacenamientoFotos) {
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.fotoRepository = fotoRepository;
        this.almacenamientoFotos = almacenamientoFotos;
    }

    public LugarResponse crearLugar(LugarRequest request, Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para crear un lugar");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Point ubicacion = GeometryUtils.createPoint(request.lat(), request.lon());

        Lugar lugar = new Lugar(
                null,
                request.nombre(),
                request.descripcion(),
                normalizarCategoria(request.categoria()),
                ubicacion,
                usuario,
                Instant.now()
        );

        Lugar guardado = lugarRepository.save(lugar);
        return toResponse(guardado, null);
    }

    @Transactional(readOnly = true)
    public LugarResponse obtenerPorId(Long id) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));
        Double media = comentarioRepository.calcularMediaValoracion(id).orElse(null);
        long total = comentarioRepository.countByLugarId(id);
        return toResponse(lugar, null, media, total);
    }

    @Transactional(readOnly = true)
    public List<LugarResponse> listarLugares(Double lat, Double lon, Double radio, String categoria) {
        if ((lat == null) != (lon == null)) {
            throw new IllegalArgumentException("Debes enviar lat y lon juntos");
        }
        if (lat != null && (lat < -90 || lat > 90 || lon < -180 || lon > 180)) {
            throw new IllegalArgumentException("Coordenadas fuera de rango");
        }

        String categoriaFiltro = normalizarCategoria(categoria);

        if (lat != null) {
            double radioMetros = (radio != null && radio > 0) ? radio : 10_000.0;
            if (radioMetros > RADIO_MAX_METROS) {
                radioMetros = RADIO_MAX_METROS;
            }

            List<Lugar> lugares = lugarRepository.buscarCercanos(
                    lat, lon, radioMetros, categoriaFiltro, LIMITE_LISTADO);

            return lugares.stream()
                    .map(lugar -> {
                        double dist = GeometryUtils.haversineDistance(
                                lat, lon,
                                GeometryUtils.getLat(lugar.getUbicacion()),
                                GeometryUtils.getLon(lugar.getUbicacion())
                        );
                        return toResponse(lugar, dist);
                    })
                    .toList();
        }

        // shortcut: sin lat/lon devolvemos los primeros 200 por id, no los mas cercanos;
        // si la tabla crece, paginar y ordenar por fecha de creacion.
        Pageable limite = PageRequest.of(0, LIMITE_LISTADO);
        List<Lugar> lugares = (categoriaFiltro != null)
                ? lugarRepository.findByCategoria(categoriaFiltro, limite)
                : lugarRepository.findAll(limite).getContent();

        return lugares.stream()
                .map(lugar -> toResponse(lugar, null))
                .toList();
    }

    public LugarResponse editarLugar(Long id, LugarRequest request, Long usuarioId) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));

        if (usuarioId == null || !lugar.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para modificar este lugar. Solo el creador puede editarlo.");
        }

        lugar.setNombre(request.nombre());
        lugar.setDescripcion(request.descripcion());
        lugar.setCategoria(normalizarCategoria(request.categoria()));
        lugar.setUbicacion(GeometryUtils.createPoint(request.lat(), request.lon()));

        Lugar actualizado = lugarRepository.save(lugar);
        return toResponse(actualizado, null);
    }

    public void eliminarLugar(Long id, Long usuarioId) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));

        if (usuarioId == null || !lugar.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para eliminar este lugar. Solo el creador puede eliminarlo.");
        }

        // Borramos la fila primero y los archivos solo cuando la transaccion confirma.
        List<String> urls = fotoRepository.findByLugarId(id).stream()
                .map(Foto::getUrl)
                .toList();

        lugarRepository.delete(lugar);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    borrarArchivos(urls);
                }
            });
        } else {
            // Sin transaccion activa (llamadas directas o tests) borramos al momento.
            borrarArchivos(urls);
        }
    }

    private void borrarArchivos(List<String> urls) {
        for (String url : urls) {
            try {
                almacenamientoFotos.eliminar(url);
            } catch (RuntimeException e) {
                log.warn("No se pudo eliminar el archivo de la foto: {}", url, e);
            }
        }
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        return categoria.trim().toUpperCase(Locale.ROOT);
    }

    private LugarResponse toResponse(Lugar lugar, Double distanciaMetros) {
        return toResponse(lugar, distanciaMetros, null, null);
    }

    private LugarResponse toResponse(Lugar lugar, Double distanciaMetros,
                                     Double valoracionMedia, Long totalComentarios) {
        return new LugarResponse(
                lugar.getId(),
                lugar.getNombre(),
                lugar.getDescripcion(),
                lugar.getCategoria(),
                GeometryUtils.getLat(lugar.getUbicacion()),
                GeometryUtils.getLon(lugar.getUbicacion()),
                lugar.getUsuario() != null ? lugar.getUsuario().getId() : null,
                lugar.getFechaCreacion(),
                distanciaMetros,
                valoracionMedia,
                totalComentarios
        );
    }
}
