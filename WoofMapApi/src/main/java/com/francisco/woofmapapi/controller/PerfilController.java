package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.dto.PerfilRequest;
import com.francisco.woofmapapi.dto.PerfilResponse;
import com.francisco.woofmapapi.dto.PerroResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios/me")
@Tag(name = "Perfil", description = "Datos del usuario, sus lugares y las fotos de sus perros")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @Operation(summary = "Obtener mi perfil")
    public ResponseEntity<PerfilResponse> obtener(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(perfilService.obtener(principal.id()));
    }

    @PutMapping
    @Operation(summary = "Editar mi perfil", description = "Actualiza el nombre. El email no se puede cambiar.")
    public ResponseEntity<PerfilResponse> actualizar(
            @Valid @RequestBody PerfilRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(perfilService.actualizar(principal.id(), request));
    }

    @GetMapping("/lugares")
    @Operation(summary = "Mis lugares creados")
    public ResponseEntity<List<LugarResponse>> lugares(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(perfilService.lugares(principal.id()));
    }

    @GetMapping("/perros")
    @Operation(summary = "Las fotos de mis perros")
    public ResponseEntity<List<PerroResponse>> perros(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(perfilService.perros(principal.id()));
    }
}
