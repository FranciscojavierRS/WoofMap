package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/** Borrado de cualquier contenido desde el panel de administracion. */
@Service
public class AdminContenidoService {

    private final LugarService lugarService;
    private final FotoService fotoService;
    private final ComentarioService comentarioService;
    private final PerroService perroService;

    public AdminContenidoService(LugarService lugarService, FotoService fotoService,
                                 ComentarioService comentarioService, PerroService perroService) {
        this.lugarService = lugarService;
        this.fotoService = fotoService;
        this.comentarioService = comentarioService;
        this.perroService = perroService;
    }

    public void eliminar(String tipo, Long id) {
        if (!UserPrincipal.esAdministradorActual()) {
            throw new AccessDeniedException("Solo un administrador puede borrar contenido");
        }
        switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case ReporteService.TIPO_LUGAR -> lugarService.eliminarLugar(id, null);
            case ReporteService.TIPO_FOTO -> fotoService.eliminarFotoPorId(id);
            case ReporteService.TIPO_COMENTARIO -> comentarioService.eliminarComentarioPorId(id);
            default -> throw new IllegalArgumentException("Tipo de contenido no valido: " + tipo);
        }
    }
}
