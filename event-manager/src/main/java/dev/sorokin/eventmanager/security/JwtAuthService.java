package dev.sorokin.eventmanager.security;

import dev.sorokin.eventmanager.config.JwtTokenProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtAuthService {

    private final SecretKey secretKey;
    private final long expirationInMillis;

    public JwtAuthService(JwtTokenProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationInMillis = properties.expirationInMillis();
    }

    @SuppressWarnings("java:S2143")
    public String generateToken(String login) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(login)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationInMillis)))
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
