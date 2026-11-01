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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class PerroService {

    // shortcut: topes fijos sin paginar; si crece el concurso, anadir page/size.
    private static final int LIMITE_RANKING = 50;
    private static final int LIMITE_MAZO = 50;
    private static final int NOMBRE_MAX = 100;

    private final PerroRepository perroRepository;
    private final VotoPerroRepository votoPerroRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoFotos almacenamientoFotos;

    public PerroService(PerroRepository perroRepository,
                        VotoPerroRepository votoPerroRepository,
                        UsuarioRepository usuarioRepository,
                        AlmacenamientoFotos almacenamientoFotos) {
        this.perroRepository = perroRepository;
        this.votoPerroRepository = votoPerroRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoFotos = almacenamientoFotos;
    }

    public PerroResponse subirPerro(MultipartFile archivo, String nombre, Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para subir un perro");
        }

        String nombreLimpio = nombre != null ? nombre.trim() : "";
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del perro es obligatorio");
        }
        if (nombreLimpio.length() > NOMBRE_MAX) {
            throw new IllegalArgumentException("El nombre no puede superar los " + NOMBRE_MAX + " caracteres");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        String url = almacenamientoFotos.guardar(archivo);

        try {
            Perro guardado = perroRepository.save(
                    new Perro(null, nombreLimpio, url, usuario, Instant.now()));

            // Si el commit falla despues de guardar, limpiamos el archivo.
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_COMMITTED) {
                            eliminarIgnorandoErrores(url);
                        }
                    }
                });
            }

            return toResponse(guardado, 0, 0);
        } catch (RuntimeException e) {
            eliminarIgnorandoErrores(url);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<PerroResponse> ranking(String periodo) {
        Instant desde = "SEMANA".equalsIgnoreCase(periodo)
                ? Instant.now().minus(7, ChronoUnit.DAYS)
                : Instant.now().minus(30, ChronoUnit.DAYS);

        return perroRepository.ranking(desde, PageRequest.of(0, LIMITE_RANKING)).stream()
                .map(fila -> toResponse(
                        (Perro) fila[0],
                        ((Number) fila[1]).longValue(),
                        ((Number) fila[2]).longValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PerroResponse> mazo(Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para valorar perros");
        }
        return perroRepository.mazo(usuarioId, PageRequest.of(0, LIMITE_MAZO)).stream()
                .map(perro -> toResponse(perro, 0, 0))
                .toList();
    }

    public void votar(Long perroId, VotoRequest request, Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para valorar perros");
        }

        Perro perro = perroRepository.findById(perroId)
                .orElseThrow(() -> new ResourceNotFoundException("Perro no encontrado con id: " + perroId));

        if (perro.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes valorar tu propio perro");
        }
        if (votoPerroRepository.existsByPerroIdAndUsuarioId(perroId, usuarioId)) {
            throw new IllegalArgumentException("Ya has valorado a este perro");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        votoPerroRepository.save(new VotoPerro(
                null, perro, usuario, Boolean.TRUE.equals(request.positivo()), Instant.now()));
    }

    private void eliminarIgnorandoErrores(String url) {
        try {
            almacenamientoFotos.eliminar(url);
        } catch (RuntimeException ignored) {
            // No ocultamos el error original si la limpieza tambien falla.
        }
    }

    private PerroResponse toResponse(Perro perro, long likes, long dislikes) {
        return new PerroResponse(
                perro.getId(),
                perro.getNombre(),
                perro.getUrl(),
                perro.getUsuario().getId(),
                perro.getUsuario().getNombre(),
                perro.getFechaSubida(),
                likes,
                dislikes
        );
    }
}
