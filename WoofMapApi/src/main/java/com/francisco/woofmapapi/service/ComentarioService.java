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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             LugarRepository lugarRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarComentarios(Long lugarId) {
        if (!lugarRepository.existsById(lugarId)) {
            throw new ResourceNotFoundException("Lugar no encontrado con id: " + lugarId);
        }
        return comentarioRepository.findByLugarIdOrderByFechaDesc(lugarId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ComentarioResponse crearComentario(Long lugarId, ComentarioRequest request, Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para comentar");
        }

        Lugar lugar = lugarRepository.findById(lugarId)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + lugarId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Comentario comentario = new Comentario(
                null,
                request.texto(),
                request.valoracion(),
                lugar,
                usuario,
                Instant.now()
        );

        Comentario guardado = comentarioRepository.save(comentario);
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public Double calcularMediaValoracion(Long lugarId) {
        return comentarioRepository.calcularMediaValoracion(lugarId).orElse(null);
    }

    @Transactional(readOnly = true)
    public long contarComentarios(Long lugarId) {
        return comentarioRepository.countByLugarId(lugarId);
    }

    private ComentarioResponse toResponse(Comentario c) {
        return new ComentarioResponse(
                c.getId(),
                c.getTexto(),
                c.getValoracion(),
                c.getLugar().getId(),
                c.getUsuario().getId(),
                c.getFecha()
        );
    }
}
