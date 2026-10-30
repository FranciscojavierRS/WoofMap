package com.francisco.woofmapapi.service;

import com.francisco.woofmapapi.dto.AuthResponse;
import com.francisco.woofmapapi.dto.LoginRequest;
import com.francisco.woofmapapi.dto.RegisterRequest;
import com.francisco.woofmapapi.entity.Usuario;
import com.francisco.woofmapapi.exception.EmailAlreadyExistsException;
import com.francisco.woofmapapi.exception.TooManyRequestsException;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration VENTANA_INTENTOS = Duration.ofMinutes(5);
    private static final int MAX_EMAILS_EN_MEMORIA = 10_000;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Map<String, IntentosLogin> intentosFallidos = new ConcurrentHashMap<>();

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

        try {
            // saveAndFlush fuerza el INSERT aqui para poder traducir la violacion de unicidad.
            Usuario usuarioGuardado = usuarioRepository.saveAndFlush(nuevoUsuario);
            String token = jwtService.generarToken(usuarioGuardado);

            return new AuthResponse(
                    token,
                    usuarioGuardado.getId(),
                    usuarioGuardado.getNombre(),
                    usuarioGuardado.getEmail()
            );
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException("El email ya esta registrado: " + email);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        verificarLimite(email);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()
                || !passwordEncoder.matches(request.password(), usuarioOpt.get().getPasswordHash())) {
            registrarFallo(email);
            throw new BadCredentialsException("Credenciales invalidas");
        }

        intentosFallidos.remove(email);
        Usuario usuario = usuarioOpt.get();
        String token = jwtService.generarToken(usuario);

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }

    private void verificarLimite(String email) {
        IntentosLogin intentos = intentosFallidos.get(email);
        if (intentos != null
                && intentos.inicio().plus(VENTANA_INTENTOS).isAfter(Instant.now())
                && intentos.cantidad() >= MAX_INTENTOS_FALLIDOS) {
            throw new TooManyRequestsException(
                    "Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo.");
        }
    }

    private void registrarFallo(String email) {
        Instant ahora = Instant.now();

        // shortcut: límite en memoria por proceso; con varias instancias, usar Redis o limitar en el proxy.
        // Nunca vaciamos el mapa entero: al llegar al tope solo purgamos lo caducado y, si sigue
        // lleno, dejamos de registrar emails nuevos para no borrar límites vivos.
        if (intentosFallidos.size() >= MAX_EMAILS_EN_MEMORIA) {
            intentosFallidos.entrySet().removeIf(e ->
                    e.getValue().inicio().plus(VENTANA_INTENTOS).isBefore(ahora));
            if (intentosFallidos.size() >= MAX_EMAILS_EN_MEMORIA
                    && !intentosFallidos.containsKey(email)) {
                return;
            }
        }

        intentosFallidos.compute(email, (clave, actual) -> {
            if (actual == null || actual.inicio().plus(VENTANA_INTENTOS).isBefore(ahora)) {
                return new IntentosLogin(ahora, 1);
            }
            return new IntentosLogin(actual.inicio(), actual.cantidad() + 1);
        });
    }

    private record IntentosLogin(Instant inicio, int cantidad) {
    }
}
