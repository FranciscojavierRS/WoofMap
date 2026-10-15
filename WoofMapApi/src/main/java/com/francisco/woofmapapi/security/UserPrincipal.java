package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String nombre;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password, String nombre, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.authorities = authorities;
    }

    public static UserPrincipal fromUsuario(Usuario usuario) {
        return new UserPrincipal(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getNombre(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
