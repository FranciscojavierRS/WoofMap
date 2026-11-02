package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.entity.Favorito;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.FavoritoRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final LugarRepository lugarRepository;
    private final UsuarioRepository usuarioRepository;
    private final LugarService lugarService;

    public FavoritoService(FavoritoRepository favoritoRepository, LugarRepository lugarRepository,
                           UsuarioRepository usuarioRepository, LugarService lugarService) {
        this.favoritoRepository = favoritoRepository;
        this.lugarRepository = lugarRepository;
        this.usuarioRepository = usuarioRepository;
        this.lugarService = lugarService;
    }

    @Transactional(readOnly = true)
    public List<LugarResponse> listar(Long usuarioId) {
        comprobarUsuario(usuarioId);
        return favoritoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(favorito -> lugarService.aResponse(favorito.getLugar()))
                .toList();
    }

    public void anadir(Long usuarioId, Long lugarId) {
        comprobarUsuario(usuarioId);
        if (favoritoRepository.existsByUsuarioIdAndLugarId(usuarioId, lugarId)) {
            return;
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        Lugar lugar = lugarRepository.findById(lugarId)
                .orElseThrow(() -> new ResourceNotFoundException("Lugar no encontrado con id: " + lugarId));
        favoritoRepository.save(new Favorito(null, usuario, lugar, Instant.now()));
    }

    public void eliminar(Long usuarioId, Long lugarId) {
        comprobarUsuario(usuarioId);
        favoritoRepository.findByUsuarioIdAndLugarId(usuarioId, lugarId)
                .ifPresent(favoritoRepository::delete);
    }

    private void comprobarUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new AccessDeniedException("Se requiere autenticacion para usar favoritos");
        }
    }
}
