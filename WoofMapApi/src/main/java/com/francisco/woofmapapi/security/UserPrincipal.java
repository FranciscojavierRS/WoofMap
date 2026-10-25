package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(
        Long id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) {
    public static UserPrincipal fromUsuario(Usuario usuario) {
        return new UserPrincipal(
                usuario.getId(),
                usuario.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
