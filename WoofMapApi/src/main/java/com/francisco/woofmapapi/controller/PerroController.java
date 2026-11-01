package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.PerroResponse;
import com.francisco.woofmapapi.dto.VotoRequest;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.PerroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/perros")
@Tag(name = "Perros", description = "Concurso Perro del Mes/Semana y valoracion tipo Tinder")
public class PerroController {

    private final PerroService perroService;

    public PerroController(PerroService perroService) {
        this.perroService = perroService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir un perro",
            description = "Sube una imagen (JPEG, PNG o WebP, maximo 15 MB) con el nombre del perro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perro subido correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo o nombre no valido"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT")
    })
    public ResponseEntity<PerroResponse> subirPerro(
            @Parameter(description = "Archivo de imagen") @RequestParam("foto") MultipartFile foto,
            @Parameter(description = "Nombre del perro") @RequestParam("nombre") String nombre,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = principal != null ? principal.id() : null;
        PerroResponse response = perroService.subirPerro(foto, nombre, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ranking")
    @Operation(
            summary = "Ranking de perros",
            description = "Perros con mas likes en la ultima semana o en los ultimos 30 dias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking obtenido")
    })
    public ResponseEntity<List<PerroResponse>> ranking(
            @Parameter(description = "SEMANA o MES (por defecto MES)")
            @RequestParam(required = false, defaultValue = "MES") String periodo
    ) {
        return ResponseEntity.ok(perroService.ranking(periodo));
    }

    @GetMapping("/mazo")
    @Operation(
            summary = "Perros por valorar",
            description = "Devuelve perros de otros usuarios que el usuario autenticado aun no ha valorado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mazo obtenido"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT")
    })
    public ResponseEntity<List<PerroResponse>> mazo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = principal != null ? principal.id() : null;
        return ResponseEntity.ok(perroService.mazo(usuarioId));
    }

    @PostMapping("/{id}/votos")
    @Operation(
            summary = "Valorar un perro",
            description = "Like (positivo=true) o dislike (positivo=false). No se puede votar al propio perro ni dos veces."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Voto registrado"),
            @ApiResponse(responseCode = "400", description = "Voto invalido, propio o duplicado"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT"),
            @ApiResponse(responseCode = "404", description = "Perro no encontrado")
    })
    public ResponseEntity<Void> votar(
            @Parameter(description = "ID del perro") @PathVariable Long id,
            @Valid @RequestBody VotoRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = principal != null ? principal.id() : null;
        perroService.votar(id, request, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
