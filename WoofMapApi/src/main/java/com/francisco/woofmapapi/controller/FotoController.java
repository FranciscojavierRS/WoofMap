package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.FotoResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lugares/{lugarId}/fotos")
@Tag(name = "Fotos", description = "Subida y consulta de fotos asociadas a un lugar")
public class FotoController {

    private final FotoService fotoService;

    public FotoController(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir foto a un lugar",
            description = "Sube una imagen (JPEG, PNG o WebP, maximo 15 MB) asociada al lugar indicado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto subida correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo no valido (tipo, extension o tamaño)"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<FotoResponse> subirFoto(
            @Parameter(description = "ID del lugar") @PathVariable Long lugarId,
            @Parameter(description = "Archivo de imagen") @RequestParam("foto") MultipartFile foto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = principal != null ? principal.id() : null;
        FotoResponse response = fotoService.subirFoto(lugarId, foto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar fotos de un lugar",
            description = "Devuelve las fotos asociadas al lugar, ordenadas de más reciente a más antigua."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de fotos obtenida"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<List<FotoResponse>> listarFotos(
            @Parameter(description = "ID del lugar") @PathVariable Long lugarId
    ) {
        return ResponseEntity.ok(fotoService.listarFotosDeLugar(lugarId));
    }
}
