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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AuthService {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration VENTANA_INTENTOS = Duration.ofMinutes(5);
    private static final int MAX_EMAILS_EN_MEMORIA = 10_000;
    private static final int MAX_BYTES_PASSWORD = 72;
    // Hash BCrypt de una contrasena aleatoria: se compara cuando el email no existe
    // para que el tiempo de respuesta no revele si la cuenta esta registrada.
    static final String HASH_SENUELO = "$2a$10$Dc7/GinWjhmQgcck0odRD.Nf6M08cBtxS370glbb8atT.gLBmL7Ve";

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
        if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD) {
            throw new IllegalArgumentException(
                    "La contrasena no puede superar los " + MAX_BYTES_PASSWORD + " bytes");
        }
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

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        // Siempre se ejecuta BCrypt, exista o no la cuenta, para no filtrar emails por tiempo.
        String hash = usuario != null ? usuario.getPasswordHash() : HASH_SENUELO;
        boolean credencialesValidas = passwordEncoder.matches(request.password(), hash) && usuario != null;

        if (!credencialesValidas) {
            registrarFallo(email);
            if (superaLimite(email)) {
                throw new TooManyRequestsException(
                        "Demasiados intentos fallidos. Espera unos minutos antes de volver a intentarlo.");
            }
            throw new BadCredentialsException("Credenciales invalidas");
        }

        intentosFallidos.remove(email);
        String token = jwtService.generarToken(usuario);

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }

    private boolean superaLimite(String email) {
        IntentosLogin intentos = intentosFallidos.get(email);
        return intentos != null
                && intentos.inicio().plus(VENTANA_INTENTOS).isAfter(Instant.now())
                && intentos.cantidad() > MAX_INTENTOS_FALLIDOS;
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
