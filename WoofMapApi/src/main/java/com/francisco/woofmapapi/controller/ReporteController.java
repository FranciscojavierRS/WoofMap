package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.ReporteRequest;
import com.francisco.woofmapapi.dto.ReporteResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Reportes de contenido y su revision por administradores")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    @Operation(summary = "Reportar un lugar, foto o comentario")
    public ResponseEntity<ReporteResponse> crear(
            @Valid @RequestBody ReporteRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.crear(principal.id(), request));
    }

    @GetMapping
    @Operation(summary = "Listar reportes (solo administradores)")
    public ResponseEntity<List<ReporteResponse>> listar() {
        return ResponseEntity.ok(reporteService.listar());
    }

    @PutMapping("/{id}/resolver")
    @Operation(summary = "Marcar un reporte como resuelto (solo administradores)")
    public ResponseEntity<Void> resolver(@PathVariable Long id) {
        reporteService.resolver(id);
        return ResponseEntity.noContent().build();
    }
}
