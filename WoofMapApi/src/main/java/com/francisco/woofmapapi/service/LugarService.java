package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class LugarService {

    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;

    public LugarService(LugarRepository lugarRepository, UsuarioRepository usuarioRepository) {
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
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
                request.categoria(),
                ubicacion,
                usuario,
                Instant.now()
        );

        Lugar guardado = lugarRepository.save(lugar);
        return toResponse(guardado, null);
    }

    public LugarResponse crearLugar(LugarRequest request) {
        return crearLugar(request, request.usuarioId());
    }

    @Transactional(readOnly = true)
    public LugarResponse obtenerPorId(Long id) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));
        return toResponse(lugar, null);
    }

    @Transactional(readOnly = true)
    public List<LugarResponse> listarLugares(Double lat, Double lon, Double radio, String categoria) {
        if (lat != null && lon != null) {
            double radioMetros = (radio != null && radio > 0) ? radio : 10000.0; // defecto 10 km
            List<Lugar> lugares;
            if (categoria != null && !categoria.isBlank()) {
                lugares = lugarRepository.findNearbyWithCategory(lat, lon, radioMetros, categoria.trim());
            } else {
                lugares = lugarRepository.findNearby(lat, lon, radioMetros);
            }

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

        List<Lugar> lugares;
        if (categoria != null && !categoria.isBlank()) {
            lugares = lugarRepository.findByCategoria(categoria.trim());
        } else {
            lugares = lugarRepository.findAll();
        }

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
        lugar.setCategoria(request.categoria());
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

        lugarRepository.delete(lugar);
    }

    private LugarResponse toResponse(Lugar lugar, Double distanciaMetros) {
        return new LugarResponse(
                lugar.getId(),
                lugar.getNombre(),
                lugar.getDescripcion(),
                lugar.getCategoria(),
                GeometryUtils.getLat(lugar.getUbicacion()),
                GeometryUtils.getLon(lugar.getUbicacion()),
                lugar.getUsuario() != null ? lugar.getUsuario().getId() : null,
                lugar.getFechaCreacion(),
                distanciaMetros
        );
    }
}
