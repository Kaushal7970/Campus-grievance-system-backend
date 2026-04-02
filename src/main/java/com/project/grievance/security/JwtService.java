package com.project.grievance.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String issuer;
    private final SecretKey secretKey;
    private final long accessTokenTtlSeconds;

    public JwtService(
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ) {
        this.issuer = issuer;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String generateAccessToken(String userEmail, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
                    .issuer(issuer)
                    .subject(userEmail)
                .claim("roles", roles)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(exp))
                    .signWith(secretKey)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parser()
            .requireIssuer(issuer)
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token);
    }
}
