package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.ReporteRequest;
import com.francisco.woofmapapi.dto.ReporteResponse;
import com.francisco.woofmapapi.entity.Comentario;
import com.francisco.woofmapapi.entity.Foto;
import com.francisco.woofmapapi.entity.Lugar;
import com.francisco.woofmapapi.entity.Reporte;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.ResourceNotFoundException;
import com.francisco.woofmapapi.repository.ComentarioRepository;
import com.francisco.woofmapapi.repository.FotoRepository;
import com.francisco.woofmapapi.repository.LugarRepository;
import com.francisco.woofmapapi.repository.ReporteRepository;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ReporteService {

    public static final String TIPO_LUGAR = "LUGAR";
    public static final String TIPO_FOTO = "FOTO";
    public static final String TIPO_COMENTARIO = "COMENTARIO";

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final LugarRepository lugarRepository;
    private final FotoRepository fotoRepository;
    private final ComentarioRepository comentarioRepository;

    public ReporteService(ReporteRepository reporteRepository, UsuarioRepository usuarioRepository,
                          LugarRepository lugarRepository, FotoRepository fotoRepository,
                          ComentarioRepository comentarioRepository) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.lugarRepository = lugarRepository;
        this.fotoRepository = fotoRepository;
        this.comentarioRepository = comentarioRepository;
    }

    public ReporteResponse crear(Long usuarioId, ReporteRequest request) {
        String tipo = request.tipo().trim().toUpperCase(Locale.ROOT);
        Long referenciaId = request.referenciaId();

        if (reporteRepository.existsByUsuarioIdAndTipoAndReferenciaIdAndResueltoFalse(
                usuarioId, tipo, referenciaId)) {
            throw new IllegalArgumentException("Ya has reportado este contenido");
        }

        Long propietarioId = propietario(tipo, referenciaId);
        if (propietarioId.equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes reportar tu propio contenido");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Reporte guardado = reporteRepository.save(new Reporte(
                null, usuario, tipo, referenciaId, request.motivo().trim(), false, Instant.now()));
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listar() {
        comprobarAdmin();
        return reporteRepository.findByOrderByResueltoAscFechaDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public void resolver(Long reporteId) {
        comprobarAdmin();
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con id: " + reporteId));
        reporte.setResuelto(true);
        reporteRepository.save(reporte);
    }

    private Long propietario(String tipo, Long referenciaId) {
        return switch (tipo) {
            case TIPO_LUGAR -> {
                Lugar lugar = lugarRepository.findById(referenciaId)
                        .orElseThrow(() -> noEncontrado(tipo, referenciaId));
                yield lugar.getUsuario().getId();
            }
            case TIPO_FOTO -> {
                Foto foto = fotoRepository.findById(referenciaId)
                        .orElseThrow(() -> noEncontrado(tipo, referenciaId));
                yield foto.getUsuario().getId();
            }
            case TIPO_COMENTARIO -> {
                Comentario comentario = comentarioRepository.findById(referenciaId)
                        .orElseThrow(() -> noEncontrado(tipo, referenciaId));
                yield comentario.getUsuario().getId();
            }
            default -> throw new IllegalArgumentException("Tipo de contenido no valido: " + tipo);
        };
    }

    private ResourceNotFoundException noEncontrado(String tipo, Long id) {
        return new ResourceNotFoundException("Contenido no encontrado: " + tipo + " " + id);
    }

    private void comprobarAdmin() {
        if (!UserPrincipal.esAdministradorActual()) {
            throw new AccessDeniedException("Solo un administrador puede revisar los reportes");
        }
    }

    private ReporteResponse toResponse(Reporte reporte) {
        return new ReporteResponse(
                reporte.getId(),
                reporte.getTipo(),
                reporte.getReferenciaId(),
                reporte.getMotivo(),
                reporte.isResuelto(),
                reporte.getFecha(),
                reporte.getUsuario().getId(),
                reporte.getUsuario().getNombre()
        );
    }
}
