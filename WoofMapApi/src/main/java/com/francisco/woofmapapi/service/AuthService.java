package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.AuthResponse;
import com.francisco.woofmapapi.dto.LoginRequest;
import com.francisco.woofmapapi.dto.RegisterRequest;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.EmailAlreadyExistsException;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El email ya esta registrado: " + email);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Usuario nuevoUsuario = new Usuario(
                null,
                request.nombre().trim(),
                email,
                passwordHash,
                Instant.now()
        );

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        String token = jwtService.generarToken(usuarioGuardado);

        return new AuthResponse(
                token,
                usuarioGuardado.getId(),
                usuarioGuardado.getNombre(),
                usuarioGuardado.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generarToken(usuario);

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }
}
