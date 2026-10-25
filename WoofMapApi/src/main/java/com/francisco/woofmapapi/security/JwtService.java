package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:}") String secretKey,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable JWT_SECRET es obligatoria. Genera una con: openssl rand -base64 48");
        }
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;

        try {
            getSigningKey();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 bytes. Genera uno con: openssl rand -base64 48", e);
        }
    }

    public String generarToken(Usuario usuario) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esTokenValido(Claims claims, String email) {
        String subject = claims.getSubject();
        Date expiration = claims.getExpiration();
        return subject != null && subject.equalsIgnoreCase(email)
                && expiration != null && expiration.after(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < 32) {
                keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
