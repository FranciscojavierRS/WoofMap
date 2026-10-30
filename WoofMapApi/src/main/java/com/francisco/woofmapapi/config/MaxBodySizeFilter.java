package com.francisco.woofmapapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

/**
 * Corta los cuerpos JSON desmesurados antes de que Jackson los cargue en memoria.
 *
 * shortcut: solo miramos Content-Length; un cliente con Transfer-Encoding: chunked
 * puede saltarselo. Cuando se despliegue detras de un proxy, limitar ahi tambien.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MaxBodySizeFilter extends OncePerRequestFilter {

    private static final long MAX_BYTES = 1024 * 1024;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String tipo = request.getContentType();
        boolean multipart = tipo != null && tipo.toLowerCase(Locale.ROOT).startsWith("multipart/");

        // Los multipart ya tienen su propio limite en spring.servlet.multipart.
        if (!multipart && request.getContentLengthLong() > MAX_BYTES) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":413,\"error\":\"Payload Too Large\",\"message\":\"El cuerpo de la peticion es demasiado grande\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
