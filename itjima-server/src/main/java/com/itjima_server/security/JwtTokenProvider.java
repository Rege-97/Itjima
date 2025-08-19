package com.itjima_server.security;

import com.itjima_server.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Getter
    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        // HMAC-SHA 키 생성
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpirationMs);

        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add("id", user.getId())
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .claim("id", user.getId())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("id", Long.class);
    }

}
