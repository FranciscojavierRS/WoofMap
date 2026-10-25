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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class FotoService {

    private static final int LIMITE_LISTADO = 200;

    private final FotoRepository fotoRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoFotos almacenamientoFotos;

    public FotoService(FotoRepository fotoRepository,
                       LugarRepository lugarRepository,
                       UsuarioRepository usuarioRepository,
                       AlmacenamientoFotos almacenamientoFotos) {
        this.fotoRepository = fotoRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoFotos = almacenamientoFotos;
    }

    public FotoResponse subirFoto(Long lugarId, MultipartFile archivo, Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para subir fotos");
        }

        Lugar lugar = lugarRepository.findById(lugarId)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + lugarId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        String url = almacenamientoFotos.guardar(archivo);

        try {
            Foto foto = new Foto(null, url, lugar, usuario, Instant.now());
            Foto guardada = fotoRepository.save(foto);
            return toResponse(guardada);
        } catch (RuntimeException e) {
            // Si la base de datos falla, no dejamos el archivo huerfano en el disco.
            try {
                almacenamientoFotos.eliminar(url);
            } catch (RuntimeException ignored) {
                // No ocultamos el error original si la limpieza tambien falla.
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<FotoResponse> listarFotosDeLugar(Long lugarId) {
        if (!lugarRepository.existsById(lugarId)) {
            throw new ResourceNotFoundException("Lugar no encontrado con id: " + lugarId);
        }
        return fotoRepository.findByLugarIdOrderByFechaSubidaDesc(lugarId, PageRequest.of(0, LIMITE_LISTADO))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FotoResponse toResponse(Foto foto) {
        return new FotoResponse(
                foto.getId(),
                foto.getUrl(),
                foto.getLugar().getId(),
                foto.getUsuario().getId(),
                foto.getFechaSubida()
        );
    }
}
