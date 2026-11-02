package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public record UserPrincipal(
        Long id,
        String email,
        List<? extends GrantedAuthority> authorities
) {
    public static UserPrincipal fromUsuario(Usuario usuario) {
        List<SimpleGrantedAuthority> authorities = usuario.isAdministrador()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"),
                          new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(usuario.getId(), usuario.getEmail(), authorities);
    }

    /** Comprueba el rol del usuario autenticado; fuera de una peticion devuelve false. */
    public static boolean esAdministradorActual() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
