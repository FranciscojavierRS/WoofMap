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

    public LugarResponse crearLugar(LugarRequest request) {
        Usuario usuario;
        if (request.usuarioId() != null) {
            usuario = usuarioRepository.findById(request.usuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.usuarioId()));
        } else {
            // En Fase 1 (sin JWT obligatorio todavia), usar el primer usuario disponible o crear uno por defecto
            usuario = usuarioRepository.findAll().stream().findFirst()
                    .orElseGet(() -> usuarioRepository.save(new Usuario(
                            null,
                            "Usuario Predeterminado",
                            "default@woofmap.com",
                            "placeholder",
                            Instant.now()
                    )));
        }

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

    public LugarResponse editarLugar(Long id, LugarRequest request) {
        Lugar lugar = lugarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + id));

        lugar.setNombre(request.nombre());
        lugar.setDescripcion(request.descripcion());
        lugar.setCategoria(request.categoria());
        lugar.setUbicacion(GeometryUtils.createPoint(request.lat(), request.lon()));

        Lugar actualizado = lugarRepository.save(lugar);
        return toResponse(actualizado, null);
    }

    public void eliminarLugar(Long id) {
        if (!lugarRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lugar no encontrado con id: " + id);
        }
        lugarRepository.deleteById(id);
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
