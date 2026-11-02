package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.dto.PerfilRequest;
import com.francisco.woofmapapi.dto.PerfilResponse;
import com.francisco.woofmapapi.dto.PerroResponse;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final LugarService lugarService;
    private final PerroService perroService;

    public PerfilService(UsuarioRepository usuarioRepository, LugarService lugarService,
                         PerroService perroService) {
        this.usuarioRepository = usuarioRepository;
        this.lugarService = lugarService;
        this.perroService = perroService;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtener(Long usuarioId) {
        return toResponse(usuario(usuarioId));
    }

    public PerfilResponse actualizar(Long usuarioId, PerfilRequest request) {
        Usuario usuario = usuario(usuarioId);
        usuario.setNombre(request.nombre().trim());
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<LugarResponse> lugares(Long usuarioId) {
        usuario(usuarioId);
        return lugarService.listarDeUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<PerroResponse> perros(Long usuarioId) {
        usuario(usuarioId);
        return perroService.listarDeUsuario(usuarioId);
    }

    private Usuario usuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
    }

    private PerfilResponse toResponse(Usuario usuario) {
        return new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getFechaAlta(),
                usuario.isAdministrador()
        );
    }
}
