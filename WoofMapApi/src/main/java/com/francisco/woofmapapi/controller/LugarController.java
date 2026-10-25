package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.LugarRequest;
import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.LugarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/lugares")
@Tag(name = "Lugares", description = "Endpoints para la gestion y busqueda de lugares dog-friendly")
public class LugarController {

    private final LugarService lugarService;

    public LugarController(LugarService lugarService) {
        this.lugarService = lugarService;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo lugar", description = "Registra un nuevo lugar dog-friendly con coordenadas geograficas asociado al usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT")
    })
    public ResponseEntity<LugarResponse> crearLugar(
            @Valid @RequestBody LugarRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = (principal != null) ? principal.id() : null;
        LugarResponse response = lugarService.crearLugar(request, usuarioId);
        URI location = URI.create("/lugares/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un lugar", description = "Devuelve la informacion de un lugar por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<LugarResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lugarService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar lugares", description = "Permite consultar lugares cercanos a una posicion (lat, lon, radio en metros) y/o filtrar por categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de lugares obtenida correctamente")
    })
    public ResponseEntity<List<LugarResponse>> listarLugares(
            @Parameter(description = "Latitud del usuario") @RequestParam(required = false) Double lat,
            @Parameter(description = "Longitud del usuario") @RequestParam(required = false) Double lon,
            @Parameter(description = "Radio de busqueda en metros (por defecto 10000m / 10km)") @RequestParam(required = false) Double radio,
            @Parameter(description = "Filtrar por categoria (PARQUE, PLAYA, PIPICAN, etc.)") @RequestParam(required = false) String categoria
    ) {
        return ResponseEntity.ok(lugarService.listarLugares(lat, lon, radio, categoria));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar un lugar", description = "Actualiza los datos de un lugar existente (solo permitido al usuario creador).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador puede editarlo"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<LugarResponse> editarLugar(
            @PathVariable Long id,
            @Valid @RequestBody LugarRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = (principal != null) ? principal.id() : null;
        return ResponseEntity.ok(lugarService.editarLugar(id, request, usuarioId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un lugar", description = "Elimina un lugar por su identificador (solo permitido al usuario creador).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado: se requiere token JWT"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo el creador puede eliminarlo"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<Void> eliminarLugar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long usuarioId = (principal != null) ? principal.id() : null;
        lugarService.eliminarLugar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
