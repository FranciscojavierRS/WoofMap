package com.francisco.woofmapapi.controller;

import com.francisco.woofmapapi.dto.AuthResponse;
import com.francisco.woofmapapi.dto.LoginRequest;
import com.francisco.woofmapapi.dto.RegisterRequest;
import com.francisco.woofmapapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Endpoints para el registro e inicio de sesion de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos"),
            @ApiResponse(responseCode = "409", description = "El email ya se encuentra registrado")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica al usuario con email y contrasena y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacion exitosa"),
            @ApiResponse(responseCode = "400", description = "Formato de peticion invalido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
