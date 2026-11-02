package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.service.AdminContenidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/contenido")
@Tag(name = "Administracion", description = "Moderacion de contenido reservada a administradores")
public class AdminController {

    private final AdminContenidoService adminContenidoService;

    public AdminController(AdminContenidoService adminContenidoService) {
        this.adminContenidoService = adminContenidoService;
    }

    @DeleteMapping("/{tipo}/{id}")
    @Operation(summary = "Borrar cualquier contenido", description = "tipo: LUGAR, FOTO o COMENTARIO")
    public ResponseEntity<Void> eliminar(@PathVariable String tipo, @PathVariable Long id) {
        adminContenidoService.eliminar(tipo, id);
        return ResponseEntity.noContent().build();
    }
}
