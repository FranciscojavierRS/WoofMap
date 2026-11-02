package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.LugarResponse;
import com.francisco.woofmapapi.security.UserPrincipal;
import com.francisco.woofmapapi.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
@Tag(name = "Favoritos", description = "Lugares favoritos del usuario autenticado")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    @Operation(summary = "Listar mis lugares favoritos")
    public ResponseEntity<List<LugarResponse>> listar(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(favoritoService.listar(principal.id()));
    }

    @PostMapping("/{lugarId}")
    @Operation(summary = "Marcar un lugar como favorito")
    public ResponseEntity<Void> anadir(@PathVariable Long lugarId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        favoritoService.anadir(principal.id(), lugarId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{lugarId}")
    @Operation(summary = "Quitar un lugar de favoritos")
    public ResponseEntity<Void> eliminar(@PathVariable Long lugarId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        favoritoService.eliminar(principal.id(), lugarId);
        return ResponseEntity.noContent().build();
    }
}
