package com.bigapplication.apigateway.filter;

import com.bigapplication.apigateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationGlobalFilter.class);

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/**",
            "/api/banking/auth/**",
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/metrics/**",
            "/actuator/prometheus"
    );

    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtValidationGlobalFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (isPublicPath(path)) {
            log.info("JWT validation skipped for public path={}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "Missing bearer token");
        }

        try {
            Claims claims = extractClaims(authHeader.substring(7));
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Authenticated-User", claims.getSubject())
                    .header("X-Authenticated-Issuer", claims.getIssuer())
                    .build();
            log.debug("JWT validated for subject={} path={}", claims.getSubject(), path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException ex) {
            log.warn("JWT validation failed for path={}", path);
            return unauthorized(exchange.getResponse(), "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"success\":false,\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
