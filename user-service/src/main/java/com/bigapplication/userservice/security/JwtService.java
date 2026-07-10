package com.bigapplication.userservice.security;

import com.bigapplication.userservice.config.JwtProperties;
import com.bigapplication.userservice.dto.response.UserResponse;
import com.bigapplication.userservice.entity.User;
import com.bigapplication.userservice.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Clock clock;
    private final UserMapper userMapper;

    public JwtService(JwtProperties properties, Clock clock, UserMapper userMapper) {
        this.properties = properties;
        this.clock = clock;
        this.userMapper = userMapper;
    }

    public String generateAccessToken(UserPrincipal principal) {
        return generateToken(principal, properties.getAccessTokenExpirationMinutes(), "access");
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return generateToken(principal, properties.getRefreshTokenExpirationMinutes(), "refresh");
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        Claims claims = extractClaims(token);
        return expectedSubject.equals(claims.getSubject()) && !isExpired(claims);
    }

    public boolean isTokenType(String token, String expectedType) {
        Object tokenType = extractClaims(token).get("token_type");
        return expectedType.equals(tokenType);
    }

    public long getAccessTokenExpirationSeconds() {
        return properties.getAccessTokenExpirationMinutes() * 60;
    }

    public UserResponse toUserResponse(User user) {
        return userMapper.toResponse(user);
    }

    private String generateToken(UserPrincipal principal, long expirationMinutes, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", tokenType);
        claims.put("roles", principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(principal.getUsername())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(Date.from(clock.instant()));
    }

    private Key getSigningKey() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return key;
    }
}
