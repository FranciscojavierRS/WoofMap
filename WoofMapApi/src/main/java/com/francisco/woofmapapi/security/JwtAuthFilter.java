package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();
        try {
            Claims claims = jwtService.extraerClaims(jwt);
            String email = claims.getSubject();

            if (email != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.esTokenValido(claims, email)) {
                usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                    UserPrincipal principal = UserPrincipal.fromUsuario(usuario);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.authorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                });
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Un token invalido no autentica, pero no ocultamos los fallos de la base de datos.
            log.debug("Token JWT invalido: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
