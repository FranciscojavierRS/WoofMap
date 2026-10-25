package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.ComentarioRequest;
import com.francisco.woofmapapi.dto.ComentarioResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lugares/{lugarId}/comentarios")
@Tag(name = "Comentarios", description = "Listado y creacion de comentarios y valoraciones de un lugar")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    @Operation(
            summary = "Listar comentarios de un lugar",
            description = "Devuelve todos los comentarios del lugar ordenados del más reciente al más antiguo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de comentarios obtenida"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<List<ComentarioResponse>> listarComentarios(
            @Parameter(description = "ID del lugar") @PathVariable Long lugarId
    ) {
        return ResponseEntity.ok(comentarioService.listarComentarios(lugarId));
    }

    @PostMapping
    @Operation(
            summary = "Crear comentario y valoracion",
            description = "Publica un comentario con valoracion (1-5) en el lugar indicado. Requiere autenticacion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<ComentarioResponse> crearComentario(
            @Parameter(description = "ID del lugar") @PathVariable Long lugarId,
            @Valid @RequestBody ComentarioRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = principal != null ? principal.id() : null;
        ComentarioResponse response = comentarioService.crearComentario(lugarId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
