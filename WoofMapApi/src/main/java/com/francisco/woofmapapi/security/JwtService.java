package com.francisco.woofmapapi.security;

import com.francisco.woofmapapi.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secretKey,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", usuario.getId());
        extraClaims.put("nombre", usuario.getNombre());
        return generarToken(extraClaims, usuario.getEmail());
    }

    public String generarToken(Map<String, Object> extraClaims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    public Long extraerUsuarioId(String token) {
        Object idObj = extraerClaims(token).get("id");
        if (idObj instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public boolean esTokenValido(String token, String email) {
        try {
            Claims claims = extraerClaims(token);
            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            return subject != null && subject.equalsIgnoreCase(email) && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
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
