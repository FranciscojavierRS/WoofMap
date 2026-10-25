package com.francisco.woofmapapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.francisco.woofmapapi.exception.ErrorResponse;
import com.francisco.woofmapapi.repository.UsuarioRepository;
import com.francisco.woofmapapi.security.JwtAuthFilter;
import com.francisco.woofmapapi.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        return new JwtAuthFilter(jwtService, usuarioRepository);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse error = new ErrorResponse(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized",
                                    "No autorizado: se requiere un token JWT valido para acceder a este recurso",
                                    request.getRequestURI()
                            );
                            objectMapper.writeValue(response.getOutputStream(), error);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse error = new ErrorResponse(
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden",
                                    accessDeniedException.getMessage() != null
                                            ? accessDeniedException.getMessage()
                                            : "Acceso denegado: permisos insuficientes para esta operacion",
                                    request.getRequestURI()
                            );
                            objectMapper.writeValue(response.getOutputStream(), error);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // OpenAPI / Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Archivos de imagen servidos publicamente
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        // Autenticacion publica
                        .requestMatchers("/auth/**").permitAll()
                        // Consultas publicas de lugares y comentarios
                        .requestMatchers(HttpMethod.GET, "/lugares/**").permitAll()
                        // Creacion, edicion y borrado de lugares protegidos
                        .requestMatchers(HttpMethod.POST, "/lugares").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/lugares/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/lugares/**").authenticated()
                        // Cualquier otra peticion requiere autenticacion
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
